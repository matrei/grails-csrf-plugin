package io.github.matrei.grailsplugin.csrf

import java.security.Principal

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

import spock.lang.Specification

class SpringSecurityCsrfUserResolverSpec extends Specification {

    SpringSecurityCsrfUserResolver resolver = new SpringSecurityCsrfUserResolver()
    MockHttpServletRequest request = new MockHttpServletRequest()

    void cleanup() {
        SecurityContextHolder.clearContext()
    }

    void 'the authenticated user is resolved from the security context'() {
        given: 'an authenticated user in the security context, but not in the request'
            SecurityContextHolder.context.authentication = UsernamePasswordAuthenticationToken.authenticated(
                    'alice', 'password', AuthorityUtils.createAuthorityList('ROLE_USER'))

        expect:
            resolver.currentUser(request) == 'alice'
    }

    void 'anonymous and unauthenticated users fall back to the request user principal'(String requestUser) {
        given: 'a request user principal'
            request.userPrincipal = requestUser ? { -> requestUser } as Principal : null

        and: 'an anonymous user in the security context'
            SecurityContextHolder.context.authentication = new AnonymousAuthenticationToken(
                    'key', 'anonymousUser', AuthorityUtils.createAuthorityList('ROLE_ANONYMOUS'))

        expect:
            resolver.currentUser(request) == requestUser

        when: 'the user in the security context is not authenticated'
            SecurityContextHolder.context.authentication = UsernamePasswordAuthenticationToken.unauthenticated('bob', 'password')

        then:
            resolver.currentUser(request) == requestUser

        when: 'the security context is empty'
            SecurityContextHolder.clearContext()

        then:
            resolver.currentUser(request) == requestUser

        where:
            requestUser << [null, 'carol']
    }
}
