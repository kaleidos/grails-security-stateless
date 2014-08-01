package net.kaleidos.grails.plugin.security.stateless.filter


import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.springframework.security.core.AuthenticationException
import org.springframework.web.filter.GenericFilterBean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.AuthenticationDetailsSource
import org.springframework.security.authentication.AuthenticationManager


import net.kaleidos.grails.plugin.security.stateless.token.StatelessAuthenticationToken
import net.kaleidos.grails.plugin.security.stateless.StatelessService

import grails.converters.JSON



import grails.util.Holders as CH

class StatelessLoginFilter extends GenericFilterBean {

    boolean active

    String usernameField
    String passwordField
    String endpointUrl

    AuthenticationManager authenticationManager
    AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource


    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = request as HttpServletRequest
        HttpServletResponse httpServletResponse = response as HttpServletResponse

        if (active) {

            def actualUri =  httpServletRequest.requestURI - httpServletRequest.contextPath

            logger.debug "Actual URI is ${actualUri}; endpoint URL is ${endpointUrl}"

            //Only apply filter to the configured URL
            if (actualUri == endpointUrl) {
                log.debug "Applying authentication filter to this request"

                //Only POST is supported
                if (httpServletRequest.method != 'POST') {
                    log.debug "${httpServletRequest.method} HTTP method is not supported. Setting status to ${HttpServletResponse.SC_METHOD_NOT_ALLOWED}"
                    httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
                    return
                }
            }

            String principal = request.getParameter(usernameField)
            String credentials = request.getParameter(passwordField)


             //Request must contain parameters
            if (!principal || !credentials) {
                log.debug "Username and/or password parameters are missing. Setting status to ${HttpServletResponse.SC_BAD_REQUEST}"
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST) //400
                return
            }

            UsernamePasswordAuthenticationToken authenticationRequest = new UsernamePasswordAuthenticationToken(principal, credentials)

            authenticationRequest.details = authenticationDetailsSource.buildDetails(httpServletRequest)

            try {
                log.debug "Trying to authenticate the request"
                def authenticationResult = authenticationManager.authenticate(authenticationRequest)

                if (authenticationResult.authenticated) {
                    log.debug "Request authenticated. Storing the authentication result in the security context"
                    log.debug "Authentication result: ${authenticationResult}"

                    String tokenValue = StatelessService.generateToken(principal)
                    log.debug "Generated token: ${tokenValue}"

                    httpServletResponse.setContentType("application/json");
                    httpServletResponse << (["token": "$tokenValue"] as JSON).toString()
                    return
                }

            } catch (AuthenticationException ae) {
                log.debug "Authentication failed: ${ae.message}"
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED) //401
                return
            }

        } else {
            chain.doFilter(request, response)
        }

    }
}
