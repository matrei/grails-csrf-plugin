package io.github.matrei.grailsplugin.csrf

import java.security.Principal

import groovy.transform.CompileStatic

import org.springframework.mock.web.MockHttpServletRequest

import spock.lang.Specification

class CsrfSessionHandlerSpec extends Specification {

    CsrfConfig config = new CsrfConfig()
    CsrfSessionHandler handler = new CsrfSessionHandler(config, new UuidCsrfTokenHandler())

    void 'loading a token does not create a session or a token'() {
        given: 'a request without a session'
            var request = new MockHttpServletRequest()

        expect: 'there is no token'
            handler.loadToken(request) == null

        and: 'no session was created'
            request.getSession(false) == null
    }

    void 'a token is created once and then reused'() {
        given: 'a request without a session'
            var request = new MockHttpServletRequest()

        when: 'a token is requested twice'
            var first = handler.loadOrCreateToken(request)
            var second = handler.loadOrCreateToken(request)

        then: 'the same token is returned and stored in the session'
            first
            first == second
            request.session.getAttribute(config.attributeName) == first
            handler.loadToken(request) == first
    }

    void 'an empty stored token is replaced'() {
        given: 'a session with an empty token'
            var request = new MockHttpServletRequest()
            request.session.setAttribute(config.attributeName, '')

        expect: 'a new token is created'
            handler.loadOrCreateToken(request)
    }

    void 'the token is rotated when the user changes'(String before, String after, boolean rotated) {
        given: 'a token issued to the user before'
            var request = new MockHttpServletRequest(userPrincipal: principal(before))
            var token = handler.loadOrCreateToken(request)

        when: 'the user changes'
            request.userPrincipal = principal(after)

        then: 'the old token is only valid for the same user'
            (handler.loadToken(request) == null) == rotated

        and: 'a new token is only created for another user'
            (handler.loadOrCreateToken(request) != token) == rotated

        where:
            before  | after   | rotated
            null    | null    | false
            'alice' | 'alice' | false
            null    | 'alice' | true  // login
            'alice' | 'bob'   | true  // another login
            'alice' | null    | true  // logout
    }

    void 'clearing the token makes the next token a new one'() {
        given: 'a stored token'
            var request = new MockHttpServletRequest()
            var token = handler.loadOrCreateToken(request)

        when: 'the token is cleared'
            handler.clearToken(request)

        then: 'there is no token'
            handler.loadToken(request) == null

        and: 'the next token is a new one'
            handler.loadOrCreateToken(request) != token
    }

    void 'clearing the token does not create a session'() {
        given: 'a request without a session'
            var request = new MockHttpServletRequest()

        when: 'the token is cleared'
            handler.clearToken(request)

        then: 'no session was created'
            request.getSession(false) == null
    }

    @CompileStatic
    private static Principal principal(String name) {
        name ? { -> name } as Principal : null
    }
}
