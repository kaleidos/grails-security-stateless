package net.kaleidos.grails.plugin.security.stateless.provider

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import net.kaleidos.grails.plugin.security.stateless.token.StatelessAuthenticationToken
import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.AuthenticationException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.util.Assert
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.GrantedAuthority
import grails.plugin.springsecurity.SpringSecurityUtils

import net.kaleidos.grails.plugin.security.stateless.StatelessService






public class StatelessAuthenticationProvider implements AuthenticationProvider {
    def userDetailsService


    Authentication authenticate(Authentication authentication) throws AuthenticationException{

        Assert.isInstanceOf(StatelessAuthenticationToken, authentication, "Only StatelessAuthenticationToken is supported")
        StatelessAuthenticationToken authenticationRequest = authentication
        StatelessAuthenticationToken authenticationResult = new StatelessAuthenticationToken(authenticationRequest.tokenValue)

        if (authenticationRequest.tokenValue) {
            log.debug "Trying to validate token ${authenticationRequest.tokenValue}"


            def securityStatelessMap = StatelessService.validateAndExtractToken(authenticationRequest.tokenValue)

            println securityStatelessMap

            if (securityStatelessMap) {
                def userDetails = userDetailsService.loadUserByUsername(securityStatelessMap.username, true)
                log.debug "Authentication result: ${authenticationResult}"
                authenticationResult = new StatelessAuthenticationToken(userDetails, userDetails.password, userDetails.authorities, authenticationRequest.tokenValue)
                authenticationResult.securityStatelessMap = securityStatelessMap
            } else {
                throw new BadCredentialsException("Token invalid")
            }
        }

        return authenticationResult
    }



    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return (StatelessAuthenticationToken.class.isAssignableFrom(authentication));
    }


}
