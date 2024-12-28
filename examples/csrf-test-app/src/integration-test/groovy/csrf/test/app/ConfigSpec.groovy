package csrf.test.app

import grails.testing.mixin.integration.Integration
import io.github.matrei.grailsplugin.csrf.CsrfConfig
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.IgnoreIf
import spock.lang.Specification

@Integration
@IgnoreIf({ System.getProperty('grails.env') != 'configtest' })
class ConfigSpec extends Specification {

    @Autowired
    CsrfConfig csrfConfig

    void 'the config is loaded'() {
        expect:
        csrfConfig.fieldName == 'custom-csrf-field-name'
        !csrfConfig.cookie.secure
    }
}
