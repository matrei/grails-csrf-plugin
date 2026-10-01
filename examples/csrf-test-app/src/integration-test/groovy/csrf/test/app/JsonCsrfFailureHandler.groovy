package csrf.test.app

import groovy.transform.CompileStatic

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import io.github.matrei.grailsplugin.csrf.CsrfFailureHandler
import io.github.matrei.grailsplugin.csrf.CsrfFailureReason

@CompileStatic
class JsonCsrfFailureHandler implements CsrfFailureHandler {

    @Override
    void handle(HttpServletRequest request, HttpServletResponse response, CsrfFailureReason reason) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = 'application/json'
        response.writer.write("{\"reason\":\"${reason.name()}\"}")
    }
}
