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
package io.github.matrei.grailsplugin.csrf.taglib

import groovy.transform.CompileStatic

import grails.gsp.Tag

import io.github.matrei.grailsplugin.csrf.CsrfConfig
import io.github.matrei.grailsplugin.csrf.CsrfSessionHandler
import io.github.matrei.grailsplugin.csrf.CsrfTokenMasking

/**
 * Tag library for CSRF protection.
 *
 * @author Mattias Reichel
 * @since 1.0.0
 */
@CompileStatic
class CsrfTagLib {

    static namespace = 'csrf'

    private final CsrfConfig csrfConfig
    private final CsrfSessionHandler sessionHandler

    CsrfTagLib(CsrfConfig csrfConfig, CsrfSessionHandler sessionHandler) {
        this.csrfConfig = csrfConfig
        this.sessionHandler = sessionHandler
    }

    /**
     * Renders a meta tag with the CSRF token.
     * The token is masked differently every time it is rendered.
     */
    @Tag
    void headToken() {
        var token = CsrfTokenMasking.mask(sessionHandler.loadOrCreateToken(request))
        out << "<meta name=\"csrf-token\" content=\"$token\"/>"
    }

    /**
     * Renders a hidden input field with the CSRF token.
     * The token is masked differently every time it is rendered.
     */
    @Tag
    void formToken() {
        var token = CsrfTokenMasking.mask(sessionHandler.loadOrCreateToken(request))
        out << "<input type=\"hidden\" name=\"$csrfConfig.fieldName\" value=\"$token\"/>"
    }
}
