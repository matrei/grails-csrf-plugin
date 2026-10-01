package build.config

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class GrailsPlugin implements Plugin<Project>, GrailsProject {

    @Override
    void apply(Project project) {
        configureProjectVersion(project)
        project.pluginManager.apply('org.apache.grails.gradle.grails-plugin')
        // Packages a description of the plugin's tag libraries (META-INF/grails/taglibs) in the jar.
        // Without it, apps cannot compile namespaced calls to the plugin's tags into direct invocations
        // or report misspelled tag names at compile time; the tags then only resolve dynamically at runtime.
        project.pluginManager.apply('org.apache.grails.gradle.grails-gsp')
        project.pluginManager.apply('build.config.java')
        project.pluginManager.apply('build.config.reproducible')
        project.pluginManager.apply('build.config.test')
    }
}
