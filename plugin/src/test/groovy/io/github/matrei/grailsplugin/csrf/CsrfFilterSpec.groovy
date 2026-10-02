package io.github.matrei.grailsplugin.csrf

import java.security.Principal

import jakarta.servlet.DispatcherType
import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.Cookie

import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

import spock.lang.Specification

class CsrfFilterSpec extends Specification {

    CsrfConfig config = new CsrfConfig()
    CsrfSessionHandler sessionHandler = new CsrfSessionHandler(config, new UuidCsrfTokenHandler())
    CsrfFilter filter = new CsrfFilter(config, sessionHandler, new UuidCsrfTokenHandler(), new ForbiddenCsrfFailureHandler())
    MockHttpServletRequest request = new MockHttpServletRequest(method: 'POST', requestURI: '/')
    MockHttpServletResponse response = new MockHttpServletResponse()

    void 'read requests pass'(String httpMethod) {
        given: 'a read request without a token'
            request.method = httpMethod

        expect: 'it passes'
            passes()

        where:
            httpMethod << ['GET', 'HEAD', 'OPTIONS']
    }

    void 'write requests without a token are rejected'(String httpMethod) {
        given: 'a write request without a token'
            request.method = httpMethod

        expect: 'it is rejected'
            !passes()
            response.status == 403

        where:
            httpMethod << ['POST', 'PUT', 'PATCH', 'DELETE']
    }

    void 'excluded uris pass'(List<String> excluded, String uri, boolean passing) {
        given: 'excluded uris'
            config.excluded = excluded

        and: 'a write request without a token'
            request.requestURI = uri

        expect: 'only excluded uris pass'
            passes() == passing

        where:
            excluded               | uri                     | passing
            ['/test']              | '/test'                 | true
            ['/test']              | '/testing'              | false
            ['/test', '/testing']  | '/testing'              | true
            ['/test', '/testing']  | '/test'                 | true
            ['^/webhooks/.*']      | '/webhooks/stripe'      | true
            ['^/webhooks/.*']      | '/myapp/webhooks/hello' | false
    }

    void 'error dispatches are not checked'() {
        given: 'a write request without a token, dispatched to the error page'
            request.dispatcherType = DispatcherType.ERROR
            request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, '/books')

        expect: 'it passes'
            passes()
    }

    void 'the xsrf cookie is host-only unless a domain is configured'(String domain, String expectedDomainAttribute) {
        given: 'a session with a token'
            request.session.setAttribute(config.attributeName, 'abc')

        and: 'an enabled cookie with a configured domain'
            config.cookie.enabled = true
            config.cookie.domain = domain

        when: 'a read request comes in'
            request.method = 'GET'
            passes()

        then: 'the cookie has the expected domain attribute'
            var cookie = response.getHeader('Set-Cookie')
            cookie.startsWith('XSRF-TOKEN=abc')
            expectedDomainAttribute ? cookie.contains(expectedDomainAttribute) : !cookie.contains('Domain=')

        where:
            domain        | expectedDomainAttribute
            null          | null
            'example.com' | 'Domain=example.com'
    }

    void 'write requests are rejected without a stored token, whatever the validator says'(String tokenInStorage) {
        given: 'a validator that accepts any token'
            filter = new CsrfFilter(
                    config,
                    sessionHandler,
                    { String stored, String fromRequest -> true } as CsrfTokenValidator,
                    new ForbiddenCsrfFailureHandler()
            )

        and: 'a session with a missing or empty token'
            request.session.setAttribute(config.attributeName, tokenInStorage)

        expect: 'a write request without a token is rejected'
            !passes()
            response.status == 403

        where:
            tokenInStorage << [null, '']
    }

    void 'read requests do not create a session when the cookie is disabled'() {
        given: 'a read request'
            request.method = 'GET'

        expect: 'it passes'
            passes()

        and: 'no session or cookie is created'
            request.getSession(false) == null
            response.getHeader('Set-Cookie') == null
    }

    void 'write requests without a session are rejected without creating one'() {
        expect: 'a write request without a session is rejected'
            !passes()
            response.status == 403

        and: 'no session is created'
            request.getSession(false) == null
    }

    void 'the cookie creates a token and is only sent when the browser does not have it'() {
        given: 'an enabled cookie'
            config.cookie.enabled = true

        when: 'a read request comes in without a session'
            request.method = 'GET'
            passes()

        then: 'a token is created and sent in the cookie'
            var token = sessionHandler.loadToken(request)
            token
            response.getHeader('Set-Cookie').startsWith("XSRF-TOKEN=${token};")

        when: 'the next request already carries the current token'
            response = new MockHttpServletResponse()
            request.cookies = new Cookie('XSRF-TOKEN', token)
            passes()

        then: 'the cookie is not sent again'
            response.getHeader('Set-Cookie') == null

        when: 'the browser has a stale token'
            request.cookies = new Cookie('XSRF-TOKEN', 'stale')
            passes()

        then: 'the cookie is sent again'
            response.getHeader('Set-Cookie').startsWith("XSRF-TOKEN=${token};")
    }

    void 'a token issued before login is only accepted while the user is unchanged'(String user, boolean accepted) {
        given: 'a token issued to an anonymous user'
            var token = sessionHandler.loadOrCreateToken(request)

        when: 'the token is submitted by the current user'
            request.userPrincipal = user ? { -> user } as Principal : null
            request.addHeader('X-CSRF-TOKEN', token)

        then: 'the token is only accepted if the user has not logged in since'
            passes() == accepted

        where:
            user    | accepted
            null    | true
            'alice' | false
    }

    void 'the failure handler is told why a request was rejected'(String storedToken, String sentToken, CsrfFailureReason reason) {
        given: 'a failure handler that records the reason'
            CsrfFailureReason handledReason = null
            filter = new CsrfFilter(
                    config,
                    sessionHandler,
                    new UuidCsrfTokenHandler(),
                    { request, response, CsrfFailureReason failure -> handledReason = failure } as CsrfFailureHandler
            )

        and: 'a stored token'
            if (storedToken) {
                request.session.setAttribute(config.attributeName, storedToken)
            }

        and: 'a sent token'
            if (sentToken) {
                request.addHeader('X-CSRF-TOKEN', sentToken)
            }

        when: 'the write request is filtered'
            var result = passes()

        then: 'the request only passes when the tokens match'
            result == !reason

        and: 'the handler is called with the reason'
            handledReason == reason

        and: 'the default response is not sent when the handler is replaced'
            response.status == 200

        where:
            storedToken | sentToken | reason
            null        | 'abc'     | CsrfFailureReason.MISSING_STORED_TOKEN
            'abc'       | null      | CsrfFailureReason.MISSING_REQUEST_TOKEN
            'abc'       | 'abd'     | CsrfFailureReason.INVALID_TOKEN
            'abc'       | 'abc'     | null
    }

    void 'tokens are read from the form field and both headers'(String parameter, String header, String value) {
        given: 'a stored token'
            request.session.setAttribute(config.attributeName, 'abc')

        and: 'the token sent in a form field or header'
            if (parameter) {
                request.addParameter(parameter, value)
            }
            if (header) {
                request.addHeader(header, value)
            }

        expect: 'it passes'
            passes()

        where:
            parameter | header         | value
            '_token'  | null           | CsrfTokenMasking.mask('abc')
            null      | 'X-CSRF-TOKEN' | CsrfTokenMasking.mask('abc')
            null      | 'X-XSRF-TOKEN' | 'abc'
    }

    void 'masked and raw tokens are accepted, other tokens are not'(String sentToken, boolean accepted) {
        given: 'a stored token'
            request.session.setAttribute(config.attributeName, 'abc')

        and: 'a sent token'
            request.addHeader('X-CSRF-TOKEN', sentToken)

        expect: 'only the stored token, masked or not, is accepted'
            passes() == accepted

        where:
            sentToken                       | accepted
            CsrfTokenMasking.mask('abc')    | true
            'abc'                           | true
            CsrfTokenMasking.mask('abd')    | false
            CsrfTokenMasking.mask('abcabc') | false
            'abd'                           | false
    }

    private boolean passes() {
        var chain = new MockFilterChain()
        filter.doFilter(request, response, chain)
        chain.request != null
    }
}
