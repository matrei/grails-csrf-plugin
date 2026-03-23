package build.config

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class Test implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.tasks.withType(org.gradle.api.tasks.testing.Test).configureEach { testTask ->
            testTask.useJUnitPlatform()
            testTask.testLogging {
                events('passed', 'skipped', 'failed', 'standardOut', 'standardError')
            }
            testTask.systemProperty(
                    'grails.env',
                    System.getProperty('grails.env', 'test')
            )
        }
    }
}
