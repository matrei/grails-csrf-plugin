package csrf.test.app

import spock.lang.IgnoreIf
import spock.lang.Specification

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

import grails.testing.mixin.integration.Integration

import io.github.matrei.grailsplugin.csrf.CsrfFailureHandler

import org.apache.grails.testing.http.client.HttpClientSupport

@Integration
@IgnoreIf({ System.getProperty('grails.env') != 'test' })
@Import(CustomCsrfFailureHandlerSpec.CustomFailureHandlerConfiguration)
class CustomCsrfFailureHandlerSpec extends Specification implements HttpClientSupport {

    @TestConfiguration
    static class CustomFailureHandlerConfiguration {

        @Bean
        CsrfFailureHandler csrfFailureHandler() {
            new JsonCsrfFailureHandler()
        }
    }

    void 'an application can replace the failure handler'() {
        expect: 'a form posted without a session or token is rejected by the custom failure handler'
            httpPostForm('/', [:]).assertEquals(403, '{"reason":"MISSING_STORED_TOKEN"}')
    }
}
