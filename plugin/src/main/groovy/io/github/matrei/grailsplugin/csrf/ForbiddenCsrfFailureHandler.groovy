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
import jakarta.servlet.http.HttpServletResponse

/**
 * Default {@link CsrfFailureHandler}.
 * Responds with {@code 403 Forbidden}, which can be handled by a {@code "403"} URL mapping.
 *
 * @author Mattias Reichel
 * @since 3.0.0
 */
@CompileStatic
class ForbiddenCsrfFailureHandler implements CsrfFailureHandler {

    @Override
    void handle(HttpServletRequest request, HttpServletResponse response, CsrfFailureReason reason) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, reason.message)
    }
}
