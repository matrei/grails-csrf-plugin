/*
 * Copyright 2024-present original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.matrei.grailsplugin.csrf

import groovy.transform.CompileStatic

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import org.springframework.http.ResponseCookie
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Servlet filter for CSRF protection.
 *
 * Checks every request that is not a read request, whatever handles it: Grails controllers,
 * other servlets and other filters later in the chain. It runs after Spring Security,
 * so that the user is known for tokens bound to the user.
 *
 * @author Mattias Reichel
 * @since 3.0.0
 */
@CompileStatic
class CsrfFilter extends OncePerRequestFilter {

    /**
     * The default order of the filter, right after Spring Security's filter chain.
     */
    static final int DEFAULT_ORDER = -99

    private static final String COOKIE_XSRF = 'XSRF-TOKEN'

    private final CsrfConfig csrfConfig
    private final CsrfSessionHandler sessionHandler
    private final CsrfTokenValidator tokenValidator
    private final CsrfFailureHandler failureHandler

    CsrfFilter(
            CsrfConfig csrfConfig,
            CsrfSessionHandler sessionHandler,
            CsrfTokenValidator tokenValidator,
            CsrfFailureHandler failureHandler
    ) {
        this.csrfConfig = csrfConfig
        this.sessionHandler = sessionHandler
        this.tokenValidator = tokenValidator
        this.failureHandler = failureHandler
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var failure = RequestMethods.isReadRequest(request) || isUriExcluded(request) ? null : tokenFailure(request)
        if (failure) {
            failureHandler.handle(request, response, failure)
            return
        }
        if (csrfConfig.cookie.enabled) {
            addCookieForJs(request, response)
        }
        chain.doFilter(request, response)
    }

    private void addCookieForJs(HttpServletRequest request, HttpServletResponse response) {
        // The cookie needs a token, so this creates the session and the token if needed
        var token = sessionHandler.loadOrCreateToken(request)
        if (cookieValueFromRequest(request) == token) {
            return // The browser already has the current token
        }
        var cookie = ResponseCookie.from(COOKIE_XSRF, token)
              .path(csrfConfig.cookie.path ?: request.contextPath ?: '/')
              .domain(csrfConfig.cookie.domain) // Host-only cookie unless a domain is configured
              .secure(csrfConfig.cookie.secure ?: request.secure)
              .httpOnly(false) // This cookie is for JS consumption
              .sameSite(csrfConfig.cookie.sameSite)
              .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    private CsrfFailureReason tokenFailure(HttpServletRequest request) {
        var tokenInStorage = sessionHandler.loadToken(request)
        if (!tokenInStorage) {
            // Never accept a request when there is no stored token, whatever the validator says
            return CsrfFailureReason.MISSING_STORED_TOKEN
        }
        var tokenFromRequest = requestToken(request)
        if (!tokenFromRequest) {
            return CsrfFailureReason.MISSING_REQUEST_TOKEN
        }
        if (!isValidToken(tokenInStorage, tokenFromRequest)) {
            return CsrfFailureReason.INVALID_TOKEN
        }
        return null
    }

    private boolean isValidToken(String tokenInStorage, String tokenFromRequest) {
        // Tokens rendered in pages are masked, while the cookie token is not
        var unmaskedToken = CsrfTokenMasking.unmask(tokenFromRequest)
        (unmaskedToken && tokenValidator.validateToken(tokenInStorage, unmaskedToken)) ||
                tokenValidator.validateToken(tokenInStorage, tokenFromRequest)
    }

    private String requestToken(HttpServletRequest request) {
        request.getParameter(csrfConfig.fieldName) ?:
                request.getHeader(HttpHeaders.CSRF) ?:
                request.getHeader(HttpHeaders.XSRF)
    }

    private boolean isUriExcluded(HttpServletRequest request) {
        var uri = request.requestURI
        csrfConfig.excludedPatterns.any { uri.matches(it) }
    }

    private static String cookieValueFromRequest(HttpServletRequest request) {
        request.cookies?.find { it.name == COOKIE_XSRF }?.value
    }
}
