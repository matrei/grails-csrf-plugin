package io.github.matrei.grailsplugin.csrf

import java.util.regex.Pattern

import jakarta.servlet.http.Cookie

import spock.lang.IgnoreIf
import spock.lang.Specification

import grails.testing.web.interceptor.InterceptorUnitTest

@IgnoreIf({ System.getProperty('grails.env') != 'test' })
class CsrfInterceptorSpec extends Specification implements InterceptorUnitTest<CsrfInterceptor> {

    @Override
    Closure doWithSpring() {
        { ->
            csrfConfig(CsrfConfig)
            csrfTokenGenerator(UuidCsrfTokenHandler)
            csrfTokenValidator(UuidCsrfTokenHandler)
            csrfSessionHandler(CsrfSessionHandler, ref('csrfConfig'), ref('csrfTokenGenerator'))
        }
    }

    void setup() {
        // The config bean is shared between features, so reset what features change
        pluginConfig.cookie = new CsrfConfig.XsrfCookie()
    }

    void 'the csrf interceptor matches the intendent requests'(String uri, boolean matches) {
        when: 'a request comes in for any uri'
            withRequest(uri: uri)

        then: 'the interceptor matches'
            matches == interceptor.doesMatch()

        where:
            // Should match all requests except '/error'
            uri | matches
            '/'                   | true
            '/test'               | true
            '/static/favicon.ico' | true
            '/error'              | false
    }

    void 'uri exclusion works'(List<Pattern> excludedUris, String uri, boolean excluded) {
        when: 'testing if a uri is excluded'
            def result = interceptor._isUriExcluded(excludedUris, uri)

        then: 'the result is as expected'
            result == excluded

        where:
            excludedUris            | uri                     | excluded
            [~'/test']              | '/test'                 | true
            [~'/test']              | '/testing'              | false
            [~'/test', ~'/testing'] | '/testing'              | true
            [~'/test', ~'/testing'] | '/test'                 | true
            [~'^/webhooks/.*']      | '/webhooks/stripe'      | true
            [~'^/webhooks/.*']      | '/myapp/webhooks/hello' | false
    }

    void 'before method returns true for read requests'(String httpMethod) {
        when: 'a read request comes in'
            request.method = httpMethod
            withRequest(uri: '/')

        then: 'the before method returns true'
            interceptor.before()

        where:
            httpMethod << ['GET', 'HEAD', 'OPTIONS']
    }

    void 'before method returns false for write requests'(String httpMethod) {
        when: 'a write request comes in'
            request.method = httpMethod
            withRequest(uri: '/')

        then: 'the before method returns false'
            !interceptor.before()

        where:
            httpMethod << ['POST', 'PUT', 'PATCH', 'DELETE']
    }

    void 'the xsrf cookie is host-only unless a domain is configured'(String domain, String expectedDomainAttribute) {
        given: 'a session with a token'
            session.setAttribute(pluginConfig.attributeName, 'abc')

        and: 'an enabled cookie with a configured domain'
            pluginConfig.cookie.enabled = true
            pluginConfig.cookie.domain = domain

        when: 'a read request comes in'
            request.method = 'GET'
            withRequest(uri: '/')
            interceptor.before()

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
            var interceptor = new CsrfInterceptor(
                    pluginConfig,
                    applicationContext.getBean(CsrfSessionHandler),
                    { String stored, String fromRequest -> true } as CsrfTokenValidator
            )

        and: 'a session with a missing or empty token'
            session.setAttribute(pluginConfig.attributeName, tokenInStorage)

        when: 'a write request without a token comes in'
            request.method = 'POST'
            withRequest(uri: '/')

        then: 'the request is rejected'
            !interceptor.before()
            response.status == 403

        where:
            tokenInStorage << [null, '']
    }

    void 'read requests do not create a session when the cookie is disabled'() {
        when: 'a read request comes in'
            request.method = 'GET'
            withRequest(uri: '/')
            var result = interceptor.before()

        then: 'the request is allowed'
            result

        and: 'no session or cookie is created'
            request.getSession(false) == null
            response.getHeader('Set-Cookie') == null
    }

    void 'write requests without a session are rejected without creating one'() {
        when: 'a write request comes in without a session'
            request.method = 'POST'
            withRequest(uri: '/')
            var result = interceptor.before()

        then: 'the request is rejected'
            !result
            response.status == 403

        and: 'no session is created'
            request.getSession(false) == null
    }

    void 'the cookie creates a token and is only sent when the browser does not have it'() {
        given: 'an enabled cookie'
            pluginConfig.cookie.enabled = true

        when: 'a read request comes in without a session'
            request.method = 'GET'
            withRequest(uri: '/')
            interceptor.before()

        then: 'a token is created and sent in the cookie'
            var token = applicationContext.getBean(CsrfSessionHandler).loadToken(request)
            token
            response.getHeader('Set-Cookie').startsWith("XSRF-TOKEN=${token};")

        when: 'the next request already carries the current token'
            response.reset()
            request.cookies = new Cookie('XSRF-TOKEN', token)
            interceptor.before()

        then: 'the cookie is not sent again'
            response.getHeader('Set-Cookie') == null

        when: 'the browser has a stale token'
            request.cookies = new Cookie('XSRF-TOKEN', 'stale')
            interceptor.before()

        then: 'the cookie is sent again'
            response.getHeader('Set-Cookie').startsWith("XSRF-TOKEN=${token};")
    }

    private CsrfConfig getPluginConfig() {
        applicationContext.getBean(CsrfConfig)
    }
}
