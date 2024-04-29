package csrf.test.app
import grails.testing.mixin.integration.Integration

import geb.spock.*

@Integration
class CsrfTestAppSpec extends GebSpec {

    void 'the csrf tokens are correct'() {

        when: 'The home page is visited'
        go '/'

        then: 'All is well'
        title == 'Welcome to Grails'
        // The csrf token is in the head
        $('meta[name="csrf-token"]').size() == 1
        // The csrf token in the head is a UUID
        $('meta[name="csrf-token"]').attr('content') ==~ /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/
        // The csrf token is in the form
        $('input[name="_token"]').size() == 1
        // The csrf token in the form is a UUID
        $('input[name="_token"]').attr('value') ==~ /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/
        // The csrf token in the head and form are the same
        $('meta[name="csrf-token"]').attr('content') == $('input[name="_token"]').attr('value')
    }

}
