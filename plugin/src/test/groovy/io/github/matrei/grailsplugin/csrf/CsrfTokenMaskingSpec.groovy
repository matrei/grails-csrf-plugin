package io.github.matrei.grailsplugin.csrf

import spock.lang.Specification

class CsrfTokenMaskingSpec extends Specification {

    void 'a masked token unmasks to the token'(String token) {
        expect:
            CsrfTokenMasking.unmask(CsrfTokenMasking.mask(token)) == token

        where:
            token << [UUID.randomUUID().toString(), 'a', 'token with spaces and åäö']
    }

    void 'the same token is masked differently every time'() {
        given: 'a token'
            var token = UUID.randomUUID().toString()

        when: 'it is masked many times'
            var masked = (1..100).collect { CsrfTokenMasking.mask(token) }

        then: 'every masked value is different'
            masked.unique(false).size() == 100

        and: 'none of them contains the token'
            masked.every { !it.contains(token) }
    }

    void 'a masked token is safe in URLs, form fields and headers'() {
        expect:
            CsrfTokenMasking.mask(UUID.randomUUID().toString()) ==~ /[A-Za-z0-9_-]+/
    }

    void 'values that are not masked tokens do not unmask'(String value) {
        expect:
            CsrfTokenMasking.unmask(value) == null

        where:
            value << [
                    '',                                     // empty
                    'not base64!',                          // not Base64
                    UUID.randomUUID().toString(),           // a raw token: 27 bytes, an odd length
                    'YWJj',                                 // 'abc': an odd length
            ]
    }
}
