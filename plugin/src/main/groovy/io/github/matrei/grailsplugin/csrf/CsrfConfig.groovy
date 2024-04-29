/*
 * Copyright 2024 original authors
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
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires

import java.util.regex.Pattern

/**
 * Configuration properties for CSRF protection.
 *
 * @author Mattias Reichel
 * @since 1.0.0
  */
@CompileStatic
@ConfigurationProperties(PREFIX)
class CsrfConfig {

    private static final String PREFIX = 'csrf'

    /**
     * The name of the CSRF token input field.
     */
    String fieldName = '_token'

    /**
     * The name under with the CSRF token is stored in the session.
     */
    String attributeName = 'io.github.matrei.grailsplugin.csrf.token'

    /**
     * URIs (regexes) that should be excluded from CSRF protection.
     */
    List<String> excluded = []
    private List<Pattern> excludedPattens = []
    void setExcluded(List<String> excluded) {
        this.excluded = excluded
        excludedPattens = excluded.collect { Pattern.compile(it) }
    }
    List<Pattern> getExcludedPatterns() {
        return excludedPattens
    }

    /**
     * Settings for the optional XSRF cookie.
     */
    XsrfCookie cookie = new XsrfCookie()
    static class XsrfCookie {
        boolean enabled = true
        String path = '/'
        String domain = null
        boolean secure = true
        String sameSite = 'Lax'
    }
}
