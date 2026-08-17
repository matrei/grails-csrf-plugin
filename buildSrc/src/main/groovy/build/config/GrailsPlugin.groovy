package build.config

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.grails.gradle.plugin.core.GrailsExtension

@CompileStatic
class GrailsPlugin implements Plugin<Project>, GrailsProject {

    @Override
    void apply(Project project) {
        configureProjectVersion(project)
        configureGrailsVersion(project)
        project.pluginManager.apply('org.apache.grails.gradle.grails-plugin')
        project.pluginManager.apply('build.config.java')
        project.pluginManager.apply('build.config.reproducible')
        project.pluginManager.apply('build.config.test')
        project.extensions.configure(GrailsExtension) {
            it.springDependencyManagement = false
        }
    }
}
