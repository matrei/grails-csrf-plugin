package build.config

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class GrailsTestApp implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.version = project.findProperty('projectVersion') as String ?: '0.1.0-SNAPSHOT'
        project.pluginManager.apply('org.apache.grails.gradle.grails-web')
        project.pluginManager.apply('org.apache.grails.gradle.grails-gsp')
        project.pluginManager.apply('build.config.java')
        project.pluginManager.apply('build.config.test')
    }
}
