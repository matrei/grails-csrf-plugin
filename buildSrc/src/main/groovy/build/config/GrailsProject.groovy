package build.config

import groovy.transform.CompileStatic

import org.gradle.api.Project

@CompileStatic
trait GrailsProject {

    void configureProjectVersion(Project project) {
        def version = project.findProperty('projectVersion') as String
        if (!version) {
            throw new IllegalStateException('projectVersion property must be set for Grails plugins')
        }
        project.version = version
    }
}
