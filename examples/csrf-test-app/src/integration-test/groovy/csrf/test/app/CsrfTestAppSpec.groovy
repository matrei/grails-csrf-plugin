package csrf.test.app

import spock.lang.IgnoreIf

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration

@Integration
@IgnoreIf({ System.getProperty('grails.env') != 'test' })
class CsrfTestAppSpec extends ContainerGebSpec {

    void 'the csrf tokens are correct'() {

        when: 'The home page is visited'
            go('/')

        then: 'all is well'
            title == 'Welcome to Grails'

        and: 'the csrf token is present'
            $('meta[name="csrf-token"]').size() == 1

        and: 'the csrf token is a UUID'
            $('meta[name="csrf-token"]').attr('content') ==~ /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/

        and: 'the csrf token is in the form'
            $('input[name="_token"]').size() == 1

        and: 'the csrf token in the form is a UUID'
            $('input[name="_token"]').attr('value') ==~ /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/

        and: 'the csrf token in the head and form are the same'
            $('meta[name="csrf-token"]').attr('content') == $('input[name="_token"]').attr('value')
    }

}
