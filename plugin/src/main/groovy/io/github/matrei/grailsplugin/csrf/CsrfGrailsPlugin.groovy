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

import grails.plugins.Plugin

/**
 * Grails plugin descriptor.
 *
 * @author Mattias Reichel
 * @since 1.0.0
 */
@CompileStatic
@SuppressWarnings('unused')
class CsrfGrailsPlugin extends Plugin {

    def grailsVersion = '7.0.0 > *'
    def title = 'Grails CSRF Protection'
    def description = 'Provides CSRF protection for Grails applications.'
    def author = 'Mattias Reichel'
    def documentation = 'https://github.com/matrei/grails-csrf-plugin#readme'
    def license = 'APACHE 2.0 License'
    def issueManagement = [system: 'GitHub', url: 'https://github.com/matrei/grails-csrf-plugin/issues']
    def scm = [url: 'https://github.com/matrei/grails-csrf-plugin']

}
