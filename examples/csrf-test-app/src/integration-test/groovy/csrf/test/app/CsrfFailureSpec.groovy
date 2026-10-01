package csrf.test.app

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

import spock.lang.IgnoreIf
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Value

import grails.testing.mixin.integration.Integration

@Integration
@IgnoreIf({ System.getProperty('grails.env') != 'test' })
class CsrfFailureSpec extends Specification {

    @Value('${local.server.port}')
    Integer serverPort

    void 'a write request without a token is rejected with 403'() {
        when: 'a form is posted without a session or token'
            var response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create("http://localhost:${serverPort}/"))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            )

        then: 'the default failure handler rejects it'
            response.statusCode() == 403
    }
}
