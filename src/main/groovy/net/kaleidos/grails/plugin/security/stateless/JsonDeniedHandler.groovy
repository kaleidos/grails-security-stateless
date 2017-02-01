package net.kaleidos.grails.plugin.security.stateless

import java.io.IOException

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import groovy.util.logging.Slf4j

@Slf4j
public class JsonDeniedHandler implements AccessDeniedHandler {
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
        log.debug "Denied: $e"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.outputStream << '{"error" : "Access Denied", "message" : "' + e.message + '"}'
    }
}