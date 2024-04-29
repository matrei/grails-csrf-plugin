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
import jakarta.inject.Singleton

/**
 * Default implementation of {@link CsrfTokenGenerator} and {@link CsrfTokenValidator}.
 * Generates tokens as UUIDs.
 *
 * @author Mattias Reichel
 * @since 1.0.0
 */
@Singleton
@CompileStatic
class UuidCsrfTokenHandler implements CsrfTokenGenerator, CsrfTokenValidator {

    /**
     * Generates a UUID token.
     *
     * @return a UUID as a String
     */
    @Override
    String generateToken() {
        UUID.randomUUID().toString()
    }

    /**
     * Validates a token.
     *
     * @param tokenInStorage The valid token
     * @param tokenFromRequest The token to test
     * @return true if the token is valid
     */
    @Override
    boolean validateToken(String tokenInStorage, String tokenFromRequest) {
        tokenFromRequest == tokenInStorage
    }
}
