package csrf.test.app

import grails.testing.mixin.integration.Integration
import io.github.matrei.grailsplugin.csrf.CsrfConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import spock.lang.Specification

@Integration
@Profile('configtest')
class ConfigSpec extends Specification {

    @Autowired
    CsrfConfig csrfConfig

    void setupSpec() {
        System.setProperty('grails.env', 'configtest')
    }

    void cleanupSpec() {
        System.clearProperty('grails.env')
    }

    void 'the config is loaded'() {
        expect:
        csrfConfig.fieldName == 'custom-csrf-field-name'
        !csrfConfig.cookie.secure
    }
}
