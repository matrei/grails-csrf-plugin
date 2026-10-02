package io.github.matrei.grailsplugin.csrf

import groovy.transform.CompileStatic

import jakarta.servlet.Filter

import org.springframework.beans.factory.support.StaticListableBeanFactory
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.FilterChainProxy
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.security.web.csrf.CsrfFilter as SpringSecurityCsrfFilter
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
import org.springframework.security.web.debug.DebugFilter
import org.springframework.security.web.util.matcher.AnyRequestMatcher

import spock.lang.Specification

class SpringSecurityCsrfCheckSpec extends Specification {

    StaticListableBeanFactory beanFactory = new StaticListableBeanFactory()
    CsrfConfig config = new CsrfConfig()
    SpringSecurityCsrfCheck check = new SpringSecurityCsrfCheck(beanFactory, config)

    void 'no problems without Spring Security'() {
        expect:
            check.problems.isEmpty()
    }

    void 'Spring Security CSRF protection is reported'(boolean debug) {
        given: 'a Spring Security filter chain with CSRF protection'
            var proxy = filterChainProxy(new SpringSecurityCsrfFilter(new HttpSessionCsrfTokenRepository()))
            beanFactory.addBean('springSecurityFilterChain', debug ? new DebugFilter(proxy) : proxy)

        expect: 'it is reported'
            check.problems.size() == 1
            check.problems[0].startsWith('Spring Security CSRF protection is enabled for ')
            check.problems[0].contains('csrf.enabled: false')

        where:
            debug << [false, true]
    }

    void 'Spring Security without CSRF protection is not reported'() {
        given: 'a Spring Security filter chain without CSRF protection'
            beanFactory.addBean('springSecurityFilterChain', filterChainProxy())

        expect:
            check.problems.isEmpty()
    }

    void 'a CSRF filter that runs before Spring Security is reported'(String registration, int csrfOrder, boolean reported) {
        given: 'Spring Security registered at order -100'
            var proxy = filterChainProxy()
            beanFactory.addBean('springSecurityFilterChain', proxy)
            if (registration == 'Spring Boot') {
                beanFactory.addBean('securityFilterChainRegistration',
                        new DelegatingFilterProxyRegistrationBean('springSecurityFilterChain').tap { order = -100 })
            } else {
                beanFactory.addBean('springSecurityFilterChainRegistrationBean',
                        new FilterRegistrationBean<Filter>(proxy).tap { order = -100 })
            }

        and: 'the CSRF filter order'
            config.filter.order = csrfOrder

        expect: 'it is only reported when the CSRF filter runs first'
            (check.problems.find { it.startsWith('The CSRF filter (order ') } != null) == reported

        where:
            registration     | csrfOrder | reported
            'Spring Boot'    | -99       | false
            'Spring Boot'    | -100      | true
            'Spring Boot'    | -110      | true
            'Grails Plugin'  | -99       | false
            'Grails Plugin'  | -100      | true
    }

    @CompileStatic
    private static FilterChainProxy filterChainProxy(Filter... extraFilters) {
        var filters = [new SecurityContextHolderFilter(new HttpSessionSecurityContextRepository())] as List<Filter>
        filters.addAll(extraFilters)
        new FilterChainProxy(new DefaultSecurityFilterChain(AnyRequestMatcher.INSTANCE, filters))
    }
}
