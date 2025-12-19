/*
 * Copyright 2025-present original authors
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

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

/**
 * Auto-configuration for CSRF protection.
 *
 * @author Mattias Reichel
 * @since 2.0.0
 */
@CompileStatic
@AutoConfiguration
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CsrfConfig)
class CsrfAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    CsrfTokenGenerator csrfTokenGenerator() {
        new UuidCsrfTokenHandler()
    }

    @Bean
    @ConditionalOnMissingBean
    CsrfTokenValidator csrfTokenValidator() {
        new UuidCsrfTokenHandler()
    }

    @Lazy
    @Bean
    @ConditionalOnMissingBean
    CsrfSessionHandler csrfSessionHandler(
            CsrfConfig config,
            CsrfTokenGenerator csrfTokenGenerator) {
        new CsrfSessionHandler(
                config,
                csrfTokenGenerator
        )
    }

}
