package spring.security.app

import groovy.transform.CompileStatic

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@CompileStatic
class Application extends GrailsAutoConfiguration {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    def beans = {
        // Spring Security's own CSRF protection is disabled, as the CSRF plugin handles it.
        // It can be enabled to try out the warning the plugin logs when both are enabled.
        bean(SecurityFilterChain) { HttpSecurity http, @Value('${example.spring-security-csrf:false}') boolean springSecurityCsrf ->
            http
                    .authorizeHttpRequests { it.requestMatchers('/secure').authenticated().anyRequest().permitAll() }
                    .formLogin(Customizer.withDefaults())
                    .csrf { if (!springSecurityCsrf) it.disable() }
                    .build()
        }

        bean(UserDetailsService) {
            new InMemoryUserDetailsManager(
                    User.withUsername('alice').password('{noop}password').roles('USER').build()
            )
        }
    }
}
