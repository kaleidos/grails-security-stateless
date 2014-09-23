package net.kaleidos.grails.plugin.security.stateless.provider

import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService
import groovy.transform.CompileStatic
import net.kaleidos.grails.plugin.security.stateless.StatelessService
import net.kaleidos.grails.plugin.security.stateless.token.StatelessAuthenticationToken

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert

@CompileStatic
class StatelessAuthenticationProvider implements AuthenticationProvider {

    protected final Logger log = LoggerFactory.getLogger(getClass().name)

    GrailsUserDetailsService userDetailsService
    StatelessService statelessService

    Authentication authenticate(Authentication authentication) throws AuthenticationException {

        Assert.isInstanceOf(StatelessAuthenticationToken, authentication, "Only StatelessAuthenticationToken is supported")
        StatelessAuthenticationToken authenticationRequest = (StatelessAuthenticationToken)authentication
        StatelessAuthenticationToken authenticationResult = new StatelessAuthenticationToken(authenticationRequest.tokenValue)

        if (!authenticationRequest.tokenValue) {
            throw new BadCredentialsException("Token invalid")
        }


        log.debug "Trying to validate token ${authenticationRequest.tokenValue}"


        def securityStatelessMap = statelessService.validateAndExtractToken(authenticationRequest.tokenValue)
        if (securityStatelessMap) {
            UserDetails userDetails = userDetailsService.loadUserByUsername((String)securityStatelessMap.username, true)
            log.debug "Authentication result: ${authenticationResult}"
            authenticationResult = new StatelessAuthenticationToken(userDetails, userDetails.password, userDetails.authorities, authenticationRequest.tokenValue)
            authenticationResult.securityStatelessMap = securityStatelessMap
        }

        return authenticationResult
    }

    boolean supports(Class<?> authentication) {
        StatelessAuthenticationToken.isAssignableFrom(authentication)
    }
}
