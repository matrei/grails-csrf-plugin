package csrf.test.app

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import spock.lang.IgnoreIf
import spock.lang.Specification

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

import grails.testing.mixin.integration.Integration

import org.apache.grails.testing.http.client.HttpClientSupport
import org.apache.grails.testing.http.client.MultipartBody

@Integration
@Import(FilterCoverageSpec.PlainServletConfiguration)
@IgnoreIf({ System.getProperty('grails.env') != 'test' })
class FilterCoverageSpec extends Specification implements HttpClientSupport {

    @TestConfiguration
    static class PlainServletConfiguration {

        @Bean
        ServletRegistrationBean<PlainServlet> plainServlet() {
            new ServletRegistrationBean<PlainServlet>(new PlainServlet(), '/plain-servlet')
        }
    }

    static class PlainServlet extends HttpServlet {

        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) {
            response.writer.write('handled')
        }
    }

    void 'requests to a servlet outside Grails are protected'() {
        expect: 'read requests reach the servlet'
            http('/plain-servlet').assertEquals(200, 'handled')

        and: 'write requests without a token are rejected'
            httpPostForm('/plain-servlet', [:]).assertStatus(403)
    }

    void 'a multipart form with a masked token is accepted'(boolean validToken, int status) {
        given: 'a client that keeps the session cookie'
            var client = newHttpClientWith { cookieHandler(new CookieManager()) }

        and: 'a masked token from a rendered page'
            var page = http('/', client)
            var token = (page.body() =~ /<input type="hidden" name="_token" value="([^"]+)"/)[0][1] as String

        when: 'a multipart form with the token and a file is posted'
            var response = httpPostMultipart('/', MultipartBody.builder()
                    .addPart('_token', validToken ? token : 'tampered')
                    .addPart('file', 'hello.txt', 'text/plain', 'Hello')
                    .build(), client)

        then: 'the token is read from the multipart form'
            response.assertStatus(status)

        where:
            validToken | status
            true       | 200
            false      | 403
    }
}
