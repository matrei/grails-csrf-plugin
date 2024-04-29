package io.github.matrei.grailsplugin.csrf

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

import java.util.regex.Pattern

class CsrfInterceptorSpec extends Specification implements InterceptorUnitTest<CsrfInterceptor> {

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
}