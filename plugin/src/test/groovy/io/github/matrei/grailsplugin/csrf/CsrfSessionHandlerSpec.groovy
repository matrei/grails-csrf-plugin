package io.github.matrei.grailsplugin.csrf

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
}
