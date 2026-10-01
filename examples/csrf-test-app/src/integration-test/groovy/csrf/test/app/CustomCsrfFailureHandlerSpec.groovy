package csrf.test.app

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

import spock.lang.IgnoreIf
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

import grails.testing.mixin.integration.Integration

import io.github.matrei.grailsplugin.csrf.CsrfFailureHandler

@Integration
@IgnoreIf({ System.getProperty('grails.env') != 'test' })
@Import(CustomCsrfFailureHandlerSpec.CustomFailureHandlerConfiguration)
class CustomCsrfFailureHandlerSpec extends Specification {

    @TestConfiguration
    static class CustomFailureHandlerConfiguration {

        @Bean
        CsrfFailureHandler csrfFailureHandler() {
            new JsonCsrfFailureHandler()
        }
    }

    @Value('${local.server.port}')
    Integer serverPort

    void 'an application can replace the failure handler'() {
        when: 'a form is posted without a session or token'
            var response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create("http://localhost:${serverPort}/"))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            )

        then: 'the custom failure handler rejects it'
            response.statusCode() == 403
            response.body() == '{"reason":"MISSING_STORED_TOKEN"}'
    }
}
