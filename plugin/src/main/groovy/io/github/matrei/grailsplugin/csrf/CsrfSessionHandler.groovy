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

import org.springframework.web.util.WebUtils

/**
 * Handles CSRF token generation and storage in the session.
 * Tokens, and the sessions holding them, are only created when they are needed.
 *
 * @author Mattias Reichel
 * @since 1.0.0
 */
@CompileStatic
class CsrfSessionHandler {

    private final CsrfConfig csrfConfig
    private final CsrfTokenGenerator generator

    CsrfSessionHandler(
            CsrfConfig csrfConfig,
            CsrfTokenGenerator generator
    ) {
        this.csrfConfig = csrfConfig
        this.generator = generator
    }

    /**
     * Returns the token stored in the session, without creating a session or a token.
     *
     * @param request The current request
     * @return the stored token, or null if there is none
     */
    String loadToken(HttpServletRequest request) {
        request.getSession(false)?.getAttribute(csrfConfig.attributeName) as String
    }

    /**
     * Returns the token stored in the session, creating the session and the token if needed.
     *
     * @param request The current request
     * @return the stored token
     */
    String loadOrCreateToken(HttpServletRequest request) {
        var session = request.session
        synchronized (WebUtils.getSessionMutex(session)) {
            var token = session.getAttribute(csrfConfig.attributeName) as String
            if (!token) {
                token = generator.generateToken()
                session.setAttribute(csrfConfig.attributeName, token)
            }
            token
        }
    }
}
