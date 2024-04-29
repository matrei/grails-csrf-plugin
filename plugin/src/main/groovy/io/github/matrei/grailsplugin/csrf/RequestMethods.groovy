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

import javax.servlet.http.HttpServletRequest

/**
 * Helper for checking HTTP request methods.
 * Provides methods for determining characteristics of HTTP request methods.
 *
 * @author Mattias Reichel
 * @since 1.0.0
 */
@CompileStatic
class RequestMethods {

    static final String GET = 'GET'
    static final String HEAD = 'HEAD'
    static final String OPTIONS = 'OPTIONS'

    /**
     * Determines if the method of the given {@link HttpServletRequest} represents a read operation.
     * @param request The {@link HttpServletRequest} to be tested
     * @return true if the method of the request represents a read operation, false otherwise
     */
    static boolean isReadRequest(HttpServletRequest request) {
        return isReadRequest(request.method)
    }

    /**
     * Determine if the given request method is a read operation.
     * @param method the method to be tested e.g., 'GET', 'POST'
     * @return true if the method represents a read operation, false otherwise
     */
    static boolean isReadRequest(String method) {
        return method == GET || method == HEAD || method == OPTIONS
    }
}