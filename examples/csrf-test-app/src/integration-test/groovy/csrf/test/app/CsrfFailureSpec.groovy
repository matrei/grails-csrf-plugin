package csrf.test.app

import spock.lang.IgnoreIf
import spock.lang.Specification

import grails.testing.mixin.integration.Integration

import org.apache.grails.testing.http.client.HttpClientSupport

@Integration
@IgnoreIf({ System.getProperty('grails.env') != 'test' })
class CsrfFailureSpec extends Specification implements HttpClientSupport {

    void 'a write request without a token is rejected with 403'() {
        expect: 'a form posted without a session or token is rejected by the default failure handler'
            httpPostForm('/', [:]).assertStatus(403)
    }
}
