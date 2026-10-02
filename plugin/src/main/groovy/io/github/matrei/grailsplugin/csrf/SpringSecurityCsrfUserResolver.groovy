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

import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Resolves the user from Spring Security's security context.
 *
 * Unlike {@link HttpServletRequest#getUserPrincipal()}, the security context is available
 * wherever the token is used during the request, including in Grails tag libraries,
 * whose request is not the one Spring Security wraps.
 * Falls back to the request's user principal when there is no authenticated user.
 *
 * @author Mattias Reichel
 * @since 3.0.0
 */
@CompileStatic
class SpringSecurityCsrfUserResolver implements CsrfUserResolver {

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl()

    @Override
    String currentUser(HttpServletRequest request) {
        var authentication = SecurityContextHolder.context.authentication
        if (authentication?.authenticated && !trustResolver.isAnonymous(authentication)) {
            return authentication.name
        }
        return request.userPrincipal?.name
    }
}
