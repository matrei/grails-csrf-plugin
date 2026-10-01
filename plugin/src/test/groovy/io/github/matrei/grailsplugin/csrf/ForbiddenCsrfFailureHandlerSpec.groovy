package io.github.matrei.grailsplugin.csrf

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

import spock.lang.Specification

class ForbiddenCsrfFailureHandlerSpec extends Specification {

    void 'responds with 403 and the reason'(CsrfFailureReason reason) {
        given: 'a response'
            var response = new MockHttpServletResponse()

        when: 'a failure is handled'
            new ForbiddenCsrfFailureHandler().handle(new MockHttpServletRequest(), response, reason)

        then: 'the response is 403 with the reason message'
            response.status == 403
            response.errorMessage == reason.message

        where:
            reason << CsrfFailureReason.values()
    }
}
