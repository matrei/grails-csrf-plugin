package io.github.matrei.grailsplugin.csrf

import spock.lang.Specification

class UuidCsrfTokenHandlerSpec extends Specification {

    void 'token validation'(String tokenInStorage, String tokenFromRequest, boolean valid) {
        given: 'a token handler'
            var handler = new UuidCsrfTokenHandler()

        expect: 'only a non-empty token equal to the stored token is valid'
            handler.validateToken(tokenInStorage, tokenFromRequest) == valid

        where:
            tokenInStorage | tokenFromRequest | valid
            'abc'          | 'abc'            | true
            'abc'          | 'abd'            | false
            'abc'          | 'ab'             | false
            'abc'          | ''               | false
            'abc'          | null             | false
            null           | 'abc'            | false
            null           | ''               | false
            null           | null             | false
            ''             | ''               | false
            ''             | null             | false
    }

    void 'generated tokens validate against themselves'() {
        given: 'a token handler and a generated token'
            var handler = new UuidCsrfTokenHandler()
            var token = handler.generateToken()

        expect: 'the token is valid'
            handler.validateToken(token, token)

        and: 'a different generated token is not'
            !handler.validateToken(token, handler.generateToken())
    }
}
