package csrf.test.app

import org.openqa.selenium.Keys

import spock.lang.IgnoreIf

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration

import io.github.matrei.grailsplugin.csrf.CsrfTokenMasking

@Integration
@IgnoreIf({ System.getProperty('grails.env') != 'test' })
class CsrfTestAppSpec extends ContainerGebSpec {

    private static final String UUID_PATTERN = /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/

    void 'the csrf tokens are correct'() {

        when: 'The home page is visited'
            go('/')

        then: 'all is well'
            title == 'Welcome to Grails'

        and: 'the csrf token is present'
            $('meta[name="csrf-token"]').size() == 1

        and: 'the csrf token is a masked UUID'
            var headToken = $('meta[name="csrf-token"]').attr('content')
            CsrfTokenMasking.unmask(headToken) ==~ UUID_PATTERN

        and: 'the csrf token is in the form'
            $('input[name="_token"]').size() == 1

        and: 'the csrf token in the form is a masked UUID'
            var formToken = $('input[name="_token"]').attr('value')
            CsrfTokenMasking.unmask(formToken) ==~ UUID_PATTERN

        and: 'the csrf tokens in the head and form are masked differently'
            headToken != formToken

        and: 'they are the same token'
            CsrfTokenMasking.unmask(headToken) == CsrfTokenMasking.unmask(formToken)
    }

    void 'a form with a masked token can be submitted'() {
        given: 'the home page with a form'
            go('/')

        when: 'the form is submitted'
            $('input[name="name"]') << 'Grace' << Keys.ENTER

        then: 'the request is accepted'
            title == 'Welcome to Grails'
    }

    void 'a form with a tampered token is rejected'() {
        given: 'the home page with a form whose token has been tampered with'
            go('/')
            js.exec('document.querySelector("input[name=_token]").value = "tampered"')

        when: 'the form is submitted'
            $('input[name="name"]') << 'Grace' << Keys.ENTER

        then: 'the request is rejected'
            waitFor { title != 'Welcome to Grails' }
    }

}
