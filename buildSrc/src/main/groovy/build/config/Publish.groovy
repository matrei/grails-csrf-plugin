package build.config

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider

import org.apache.grails.gradle.publish.GrailsPublishExtension

@CompileStatic
class Publish implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.configure(GrailsPublishExtension) {
            it.title.set('Grails Plugin for CSRF protection')
            it.desc.set('Provides CSRF protection for Grails applications.')
            it.license.name = 'Apache-2.0'
            it.githubSlug.set('matrei/grails-csrf-plugin')
            it.developers.set(project.provider {
                project.findProperty('pomDevelopers') as Map ?: [:]
            } as Provider<? extends Map<? extends String, ? extends String>>)
        }
    }
}
