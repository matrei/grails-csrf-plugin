/*
 * Copyright 2024-present original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.matrei.grailsplugin.csrf

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.security.web.FilterChainProxy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CsrfFilter as SpringSecurityCsrfFilter
import org.springframework.security.web.debug.DebugFilter

/**
 * Checks, at startup, how the CSRF filter fits together with Spring Security,
 * and logs a warning for each problem found.
 *
 * @author Mattias Reichel
 * @since 3.0.0
 */
@Slf4j
@CompileStatic
class SpringSecurityCsrfCheck implements SmartInitializingSingleton {

    private static final String SPRING_SECURITY_FILTER_CHAIN = 'springSecurityFilterChain'
    private static final String SPRING_BOOT_SECURITY_FILTER_REGISTRATION = 'securityFilterChainRegistration'

    private final ListableBeanFactory beanFactory
    private final CsrfConfig csrfConfig

    SpringSecurityCsrfCheck(ListableBeanFactory beanFactory, CsrfConfig csrfConfig) {
        this.beanFactory = beanFactory
        this.csrfConfig = csrfConfig
    }

    @Override
    void afterSingletonsInstantiated() {
        problems.each { log.warn(it) }
    }

    /**
     * @return a description of each problem found, empty if there are none
     */
    List<String> getProblems() {
        var problems = []
        var csrfChains = filterChains.findAll { SecurityFilterChain chain ->
            chain.filters.any { it instanceof SpringSecurityCsrfFilter }
        }
        if (csrfChains) {
            problems << ('Spring Security CSRF protection is enabled for ' +
                    csrfChains.collect { it.toString() }.join(', ') + '. ' +
                    'Requests to these chains need two different CSRF tokens, and JavaScript requests sending ' +
                    'X-CSRF-TOKEN can only satisfy one of the checks. Disable one of them, either with ' +
                    'csrf.enabled: false or in the Spring Security configuration, or exclude these URLs in csrf.excluded.')
        }
        var securityFilterOrder = springSecurityFilterOrder
        if (securityFilterOrder != null && csrfConfig.filter.order <= securityFilterOrder) {
            problems << ("The CSRF filter (order ${csrfConfig.filter.order}) runs before Spring Security's filter chain " +
                    "(order ${securityFilterOrder}), where the logged-in user is not yet known, " +
                    'so tokens issued to logged-in users are rejected. ' +
                    "Set csrf.filter.order to a value greater than ${securityFilterOrder}.").toString()
        }
        return problems as List<String>
    }

    private List<SecurityFilterChain> getFilterChains() {
        var chains = []
        beanFactory.getBeansOfType(FilterChainProxy).values().each { chains.addAll(it.filterChains) }
        beanFactory.getBeansOfType(DebugFilter).values().each { chains.addAll(it.filterChainProxy.filterChains) }
        chains as List<SecurityFilterChain>
    }

    private Integer getSpringSecurityFilterOrder() {
        if (!beanFactory.containsBean(SPRING_SECURITY_FILTER_CHAIN)) {
            return null
        }
        var springSecurityFilter = beanFactory.getBean(SPRING_SECURITY_FILTER_CHAIN)
        // Spring Boot registers the filter by bean name, Grails Spring Security registers the filter itself
        var orders = beanFactory.getBeansOfType(DelegatingFilterProxyRegistrationBean)
                .findAll { name, registration -> name == SPRING_BOOT_SECURITY_FILTER_REGISTRATION }
                .values()*.order +
                beanFactory.getBeansOfType(FilterRegistrationBean).values()
                        .findAll { it.filter.is(springSecurityFilter) }*.order
        return orders ? orders.max() as Integer : null
    }
}
