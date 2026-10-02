package spring.security.app

import spock.lang.IgnoreIf
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

import grails.testing.mixin.integration.Integration

import io.github.matrei.grailsplugin.csrf.SpringSecurityCsrfCheck

@Integration
@IgnoreIf({ System.getProperty('grails.env') != 'test' })
@TestPropertySource(properties = ['example.spring-security-csrf=true', 'csrf.filter.order=-101'])
class SpringSecurityWarningsSpec extends Specification {

    @Autowired
    SpringSecurityCsrfCheck springSecurityCsrfCheck

    void 'both CSRF protections and a CSRF filter before Spring Security are reported'() {
        when:
            var problems = springSecurityCsrfCheck.problems

        then: 'Spring Security CSRF protection is reported'
            problems.any { it.startsWith('Spring Security CSRF protection is enabled for ') }

        and: 'the CSRF filter running before Spring Security is reported'
            problems.any { it.startsWith('The CSRF filter (order -101) runs before Spring Security\'s filter chain (order -100)') }
    }
}
