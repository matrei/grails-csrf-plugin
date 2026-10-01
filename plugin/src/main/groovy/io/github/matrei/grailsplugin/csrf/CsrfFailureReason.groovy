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

/**
 * The reason a request failed CSRF protection.
 *
 * @author Mattias Reichel
 * @since 3.0.0
 */
@CompileStatic
enum CsrfFailureReason {

    /**
     * There is no token in the session, typically because the session has expired
     * or the user has logged in or out since the token was issued.
     */
    MISSING_STORED_TOKEN('No CSRF token is stored for this session.'),

    /**
     * The request does not include a token.
     */
    MISSING_REQUEST_TOKEN('The request does not include a CSRF token.'),

    /**
     * The token in the request does not match the stored token.
     */
    INVALID_TOKEN('CSRF token mismatch.')

    /**
     * A description of the failure, suitable for an error response.
     */
    final String message

    private CsrfFailureReason(String message) {
        this.message = message
    }
}
