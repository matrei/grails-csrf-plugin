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
//import org.springframework.stereotype.Component

import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

/**
 * Handles CSRF token generation and storage in the session.
 *
 * @author Mattias Reichel
 * @since 1.0.0
 */
//@Component
@CompileStatic
class CsrfSessionHandler implements HttpSessionListener {

    private final CsrfConfig csrfConfig
    private final CsrfTokenGenerator generator

    CsrfSessionHandler(CsrfConfig csrfConfig, CsrfTokenGenerator generator) {
        this.csrfConfig = csrfConfig
        this.generator = generator
    }

    @Override
    void sessionCreated(HttpSessionEvent event) {
        event.session.setAttribute(csrfConfig.attributeName, generator.generateToken())
    }

    @Override
    void sessionDestroyed(HttpSessionEvent event) {
        // no-op
    }
}
