package net.kaleidos.grails.plugin.security.stateless.filter

import groovy.transform.CompileStatic

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import net.kaleidos.grails.plugin.security.stateless.token.StatelessAuthenticationToken
import net.kaleidos.grails.plugin.security.stateless.exception.StatelessValidationException

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.web.filter.GenericFilterBean

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

@CompileStatic
class StatelessAuthenticationFilter extends GenericFilterBean {

    AuthenticationProvider statelessAuthenticationProvider
    AuthenticationFailureHandler authenticationFailureHandler
    AccessDeniedHandler accessDeniedHandler
    boolean active

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = request as HttpServletRequest
        HttpServletResponse servletResponse = response as HttpServletResponse

        if (!active) {
            chain.doFilter(request, response)
            return
        }

        String tokenValue

        try {
            logger.debug "Looking for bearer token in Authorization header"
            if(!servletRequest.getHeader( 'Authorization')?.startsWith( 'Bearer ') ) {
                logger.debug "Token not found"
                chain.doFilter(request, response)
                return
            }

            logger.debug "Found bearer token in Authorization header"
            tokenValue = servletRequest.getHeader('Authorization').substring(7)
            logger.debug "Trying to authenticate the token"
            StatelessAuthenticationToken authenticationRequest = new StatelessAuthenticationToken(tokenValue)
            StatelessAuthenticationToken authenticationResult = statelessAuthenticationProvider.authenticate(authenticationRequest) as StatelessAuthenticationToken

            if (authenticationResult.authenticated) {
                logger.debug "Token authenticated."
                logger.debug "Authentication result: ${authenticationResult}"
                servletRequest.setAttribute 'securityStatelessMap', authenticationResult.securityStatelessMap
                SecurityContextHolder.context.setAuthentication(authenticationResult)
                chain.doFilter(request, response)
            }
        } catch (StatelessValidationException e) {
            logger.debug "Token validation failed: ${e.message}"
            accessDeniedHandler.handle(servletRequest, servletResponse, new AccessDeniedException(e.message, e))
        } catch (AuthenticationException e) {
            logger.debug "Authentication failed: ${e.message}"
            authenticationFailureHandler.onAuthenticationFailure(servletRequest, servletResponse, e)
        }
    }
}
