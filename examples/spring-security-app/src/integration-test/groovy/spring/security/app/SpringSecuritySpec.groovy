package spring.security.app

import java.net.http.HttpClient

import spock.lang.IgnoreIf
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired

import grails.testing.mixin.integration.Integration

import io.github.matrei.grailsplugin.csrf.SpringSecurityCsrfCheck

import org.apache.grails.testing.http.client.HttpClientSupport
import org.apache.grails.testing.http.client.TestHttpResponse

@Integration
@IgnoreIf({ System.getProperty('grails.env') != 'test' })
class SpringSecuritySpec extends Specification implements HttpClientSupport {

    @Autowired
    SpringSecurityCsrfCheck springSecurityCsrfCheck

    HttpClient client = newHttpClientWith { cookieHandler(new CookieManager()) }

    void 'no problems are reported when Spring Security CSRF protection is disabled'() {
        expect:
            springSecurityCsrfCheck.problems.isEmpty()
    }

    void 'a logged-in user can submit a form with the token from the page'() {
        given: 'a logged-in user'
            login()

        when: 'the secure page is rendered, rather than the login page'
            var page = http('/secure', client).assertContains(200, '<title>Secure</title>')

        then: 'its form can be submitted with its token'
            post('/secure', [_token: formToken(page), name: 'Grace']).assertStatus(200)

        and: 'not without it'
            post('/secure', [name: 'Grace']).assertStatus(403)
    }

    void 'a token issued before login is rejected after login'() {
        given: 'a token issued to an anonymous user'
            var tokenBeforeLogin = formToken(http('/', client))

        when: 'the user logs in'
            login()

        then: 'the token from before the login is rejected'
            post('/', [_token: tokenBeforeLogin, name: 'Grace']).assertStatus(403)

        and: 'a token from a page rendered after the login is accepted'
            post('/', [_token: formToken(http('/', client)), name: 'Grace']).assertStatus(200)
    }

    void 'a token issued before logout is rejected after logout'() {
        given: 'a token issued to a logged-in user'
            login()
            var tokenBeforeLogout = formToken(http('/secure', client))

        when: 'the user logs out'
            post('/logout', [:])

        then: 'the token from before the logout is rejected'
            post('/', [_token: tokenBeforeLogout, name: 'Grace']).assertStatus(403)
    }

    private TestHttpResponse post(String path, Map<String, String> form) {
        httpPostForm([:], path, form, client)
    }

    private void login() {
        // Spring Security handles its login itself, before the CSRF filter, so no token is needed
        post('/login', [username: 'alice', password: 'password']).assertStatus(200)
    }

    private static String formToken(TestHttpResponse page) {
        (page.body() =~ /<input type="hidden" name="_token" value="([^"]+)"/)[0][1]
    }
}
