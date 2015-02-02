package net.kaleidos.grails.plugin.security.stateless

import groovy.transform.CompileStatic

@CompileStatic
class StatelessValidationException extends RuntimeException {
    StatelessValidationException(String message) {
        super(message)
    }

    StatelessValidationException(String message, Throwable cause) {
        super(message, cause)
    }
}