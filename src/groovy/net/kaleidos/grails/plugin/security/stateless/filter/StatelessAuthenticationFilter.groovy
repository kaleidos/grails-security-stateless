package net.kaleidos.grails.plugin.security.stateless.filter


import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.web.filter.GenericFilterBean
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException


import net.kaleidos.grails.plugin.security.stateless.token.StatelessAuthenticationToken

class StatelessAuthenticationFilter extends GenericFilterBean {

    def statelessAuthenticationProvider
    def authenticationFailureHandler
    boolean active


    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = request as HttpServletRequest
        HttpServletResponse servletResponse = response as HttpServletResponse

        if (active) {

            String tokenValue

            log.debug "Looking for bearer token in Authorization header"
            if( servletRequest.getHeader( 'Authorization')?.startsWith( 'Bearer ') ) {
                try {
                    log.debug "Found bearer token in Authorization header"
                    tokenValue = servletRequest.getHeader( 'Authorization').substring(7)
                    log.debug "Trying to authenticate the token"
                    StatelessAuthenticationToken authenticationRequest = new StatelessAuthenticationToken(tokenValue)
                    StatelessAuthenticationToken authenticationResult = statelessAuthenticationProvider.authenticate(authenticationRequest) as StatelessAuthenticationToken

                    if (authenticationResult.authenticated) {

                            log.debug "Token authenticated."
                            log.debug "Authentication result: ${authenticationResult}"
                            servletRequest.securityStatelessMap = authenticationResult.securityStatelessMap
                            SecurityContextHolder.context.setAuthentication(authenticationResult)
                            chain.doFilter(request, response)
                    }
                } catch (AuthenticationException ae) {
                    log.debug "Authentication failed: ${ae.message}"
                    authenticationFailureHandler.onAuthenticationFailure(servletRequest, servletResponse, ae)
                }
            } else {
                log.debug "Token not found"
                authenticationFailureHandler.onAuthenticationFailure(servletRequest, servletResponse, new AuthenticationCredentialsNotFoundException("Token not found"))
            }
        } else {
            chain.doFilter(request, response)
        }

    }
}
