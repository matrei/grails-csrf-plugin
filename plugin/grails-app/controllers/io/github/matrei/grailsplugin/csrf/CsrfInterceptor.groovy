/*
 * Copyright 2024 original authors
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
import org.springframework.http.ResponseCookie

import java.util.regex.Pattern

import static io.github.matrei.grailsplugin.csrf.RequestMethods.isReadRequest

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
    private final CsrfTokenValidator tokenValidator

    CsrfInterceptor(CsrfConfig csrfConfig, CsrfTokenValidator tokenValidator) {
        matchAll().excludes(uri: '/error')
        this.csrfConfig = csrfConfig
        this.tokenValidator = tokenValidator
    }

    boolean before() {
        if (isReadRequest || uriExcluded || tokensMatch) {
            if (csrfConfig.cookie.enabled) {
                addCookieForJs()
            }
            return true
        }
        response.sendError(419, 'CSRF token mismatch.')
        return false
    }

    private void addCookieForJs() {
        def cookie = ResponseCookie.from(COOKIE_XSRF, storedToken)
              .maxAge(session.maxInactiveInterval)
              .path(csrfConfig.cookie.path ?: request.contextPath ?: '/')
              .domain(csrfConfig.cookie.domain ?: request.serverName)
              .secure(csrfConfig.cookie.secure ?: request.secure)
              .httpOnly(false) // This cookie is for JS consumption
              .sameSite(csrfConfig.cookie.sameSite)
              .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    private boolean isTokensMatch() {
        tokenValidator.validateToken(storedToken, requestToken)
    }

    private String getStoredToken() {
        session.getAttribute(csrfConfig.attributeName)
    }

    private String getRequestToken() {
        return request.getParameter(csrfConfig.fieldName) ?:
               request.getHeader(HttpHeaders.CSRF) ?:
               request.getHeader(HttpHeaders.XSRF) ?:
               ''
    }

    private boolean getIsReadRequest() {
        isReadRequest(request)
    }

    private boolean isUriExcluded() {
        _isUriExcluded(csrfConfig.excludedPatterns, request.forwardURI)
    }

    protected static boolean _isUriExcluded(List<Pattern> excluded, String uri) {
        excluded.any { uri.matches(it) }
    }
}
