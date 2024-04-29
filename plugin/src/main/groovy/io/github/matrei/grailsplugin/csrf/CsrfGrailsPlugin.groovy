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


import grails.plugins.Plugin
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * Grails plugin descriptor.
 *
 * @author Mattias Reichel
 * @since 1.0.0
 */
@CompileStatic
@SuppressWarnings('unused')
class CsrfGrailsPlugin extends Plugin {

    String grailsVersion = '6.0.0 > *'
    List<String> pluginExcludes = []
    String title = 'Grails CSRF Protection'
    String author = 'Mattias Reichel'
    String authorEmail = 'mattias.reichel@gmail.com'
    String description = 'Provides CSRF protection for Grails applications.'
    String documentation = 'https://github.com/matrei/grails-csrf-plugin#readme'
    String license = 'APACHE'

    // Any additional developers beyond the author specified above.
    // def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    Map<String,String> issueManagement = [system: 'GitHub', url: 'https://github.com/matrei/grails-csrf-plugin/issues']
    Map<String,String> scm = [url: 'https://github.com/matrei/grails-csrf-plugin']

    @Override
    @CompileDynamic
    Closure doWithSpring() {{->
        csrfSessionHandler(CsrfSessionHandler)
    }}
}