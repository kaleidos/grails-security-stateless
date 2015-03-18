package net.kaleidos.grails.plugin.security.stateless.exception

import groovy.transform.CompileStatic
import org.springframework.security.core.AuthenticationException

@CompileStatic
class ExpiredTokenException extends AuthenticationException {
    ExpiredTokenException(String message) {
        super(message)
    }

    ExpiredTokenException(String message, Throwable cause) {
        super(message, cause)
    }
}