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

import java.util.regex.Pattern

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseCookie

/**
 * Interceptor for CSRF protection.
 *
 * @author Mattias Reichel
 * @since 1.0.0
 */
@CompileStatic
class CsrfInterceptor {

    private static final String COOKIE_XSRF = 'XSRF-TOKEN'

    private final CsrfConfig csrfConfig
    private final CsrfSessionHandler sessionHandler
    private final CsrfTokenValidator tokenValidator
    private final CsrfFailureHandler failureHandler

    @Autowired
    CsrfInterceptor(
            CsrfConfig csrfConfig,
            CsrfSessionHandler sessionHandler,
            @Qualifier('csrfTokenValidator') CsrfTokenValidator tokenValidator,
            CsrfFailureHandler failureHandler
    ) {
        matchAll().excludes(uri: '/error')
        this.csrfConfig = csrfConfig
        this.sessionHandler = sessionHandler
        this.tokenValidator = tokenValidator
        this.failureHandler = failureHandler
    }

    boolean before() {
        var failure = isReadRequest || uriExcluded ? null : tokenFailure
        if (failure) {
            failureHandler.handle(request, response, failure)
            return false
        }
        if (csrfConfig.cookie.enabled) {
            addCookieForJs()
        }
        return true
    }

    private void addCookieForJs() {
        // The cookie needs a token, so this creates the session and the token if needed
        var token = sessionHandler.loadOrCreateToken(request)
        if (cookieValueFromRequest == token) {
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

    private CsrfFailureReason getTokenFailure() {
        var tokenInStorage = storedToken
        if (!tokenInStorage) {
            // Never accept a request when there is no stored token, whatever the validator says
            return CsrfFailureReason.MISSING_STORED_TOKEN
        }
        var tokenFromRequest = requestToken
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

    private String getStoredToken() {
        sessionHandler.loadToken(request)
    }

    private String getCookieValueFromRequest() {
        request.cookies?.find { it.name == COOKIE_XSRF }?.value
    }

    private String getRequestToken() {
        return request.getParameter(csrfConfig.fieldName) ?:
               request.getHeader(HttpHeaders.CSRF) ?:
               request.getHeader(HttpHeaders.XSRF) ?:
               ''
    }

    private boolean getIsReadRequest() {
        RequestMethods.isReadRequest(request)
    }

    private boolean isUriExcluded() {
        _isUriExcluded(csrfConfig.excludedPatterns, request.forwardURI)
    }

    protected static boolean _isUriExcluded(List<Pattern> excluded, String uri) {
        excluded.any { uri.matches(it) }
    }
}
