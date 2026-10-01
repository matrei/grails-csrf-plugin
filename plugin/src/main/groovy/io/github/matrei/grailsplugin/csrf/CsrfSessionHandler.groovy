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

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession

import org.springframework.web.util.WebUtils

/**
 * Handles CSRF token generation and storage in the session.
 * Tokens, and the sessions holding them, are only created when they are needed.
 *
 * A token is bound to the user it was issued to, as reported by
 * {@link HttpServletRequest#getUserPrincipal()}. When the user changes, on login or logout,
 * the token is no longer valid and a new one is created the next time one is needed.
 *
 * @author Mattias Reichel
 * @since 1.0.0
 */
@CompileStatic
class CsrfSessionHandler {

    private final CsrfConfig csrfConfig
    private final CsrfTokenGenerator generator
    private final String principalAttributeName

    CsrfSessionHandler(
            CsrfConfig csrfConfig,
            CsrfTokenGenerator generator
    ) {
        this.csrfConfig = csrfConfig
        this.generator = generator
        this.principalAttributeName = "${csrfConfig.attributeName}.principal"
    }

    /**
     * Returns the token stored in the session, without creating a session or a token.
     *
     * @param request The current request
     * @return the stored token, or null if there is none or it was issued to another user
     */
    String loadToken(HttpServletRequest request) {
        var session = request.getSession(false)
        if (!session || !isIssuedTo(session, principalName(request))) {
            return null
        }
        return session.getAttribute(csrfConfig.attributeName) as String
    }

    /**
     * Returns the token stored in the session, creating the session and the token if needed.
     * A new token is created if the stored one was issued to another user.
     *
     * @param request The current request
     * @return the stored token
     */
    String loadOrCreateToken(HttpServletRequest request) {
        var session = request.session
        var principal = principalName(request)
        synchronized (WebUtils.getSessionMutex(session)) {
            var token = session.getAttribute(csrfConfig.attributeName) as String
            if (!token || !isIssuedTo(session, principal)) {
                token = generator.generateToken()
                session.setAttribute(csrfConfig.attributeName, token)
                session.setAttribute(principalAttributeName, principal)
            }
            token
        }
    }

    /**
     * Removes the token from the session, without creating a session.
     * A new token is created the next time one is needed.
     * Call this on login and logout when the user is not reported by
     * {@link HttpServletRequest#getUserPrincipal()}.
     *
     * @param request The current request
     */
    void clearToken(HttpServletRequest request) {
        var session = request.getSession(false)
        session?.removeAttribute(csrfConfig.attributeName)
        session?.removeAttribute(principalAttributeName)
    }

    private boolean isIssuedTo(HttpSession session, String principal) {
        session.getAttribute(principalAttributeName) == principal
    }

    private static String principalName(HttpServletRequest request) {
        request.userPrincipal?.name
    }
}
