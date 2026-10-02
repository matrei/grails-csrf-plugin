package spring.security.app

import spock.lang.IgnoreIf
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource

import grails.testing.mixin.integration.Integration

import io.github.matrei.grailsplugin.csrf.SpringSecurityCsrfCheck

import org.apache.grails.testing.http.client.HttpClientSupport

@Integration
@TestPropertySource(properties = 'csrf.enabled=false')
@IgnoreIf({ System.getProperty('grails.env') != 'test' })
class CsrfDisabledSpec extends Specification implements HttpClientSupport {

    @Autowired
    ApplicationContext applicationContext

    void 'nothing is checked when CSRF protection is disabled'() {
        expect: 'a form posted without a token is accepted'
            httpPostForm('/', [name: 'Grace']).assertStatus(200)

        and: 'no token is rendered'
            http('/').assertNotContains(200, '_token')

        and: 'neither the filter nor the Spring Security check is registered'
            !applicationContext.containsBean('csrfFilterRegistration')
            applicationContext.getBeansOfType(SpringSecurityCsrfCheck).isEmpty()
    }
}
