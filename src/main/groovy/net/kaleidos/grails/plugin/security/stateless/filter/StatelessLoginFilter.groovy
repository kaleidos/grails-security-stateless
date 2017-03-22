package net.kaleidos.grails.plugin.security.stateless.filter

import grails.converters.JSON

import groovy.transform.CompileStatic

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import net.kaleidos.grails.plugin.security.stateless.token.StatelessTokenProvider
import net.kaleidos.grails.plugin.security.stateless.provider.UserSaltProvider

import org.springframework.security.authentication.AuthenticationDetailsSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.web.filter.GenericFilterBean

import groovy.json.JsonSlurper
import groovy.json.JsonException

@CompileStatic
class StatelessLoginFilter extends GenericFilterBean {

    boolean active
    boolean shouldInvalidateAfterNewToken

    String usernameField
    String passwordField
    String endpointUrl

    AuthenticationManager authenticationManager
    AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource

    StatelessTokenProvider statelessTokenProvider
    UserSaltProvider userSaltProvider

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = request as HttpServletRequest
        HttpServletResponse httpServletResponse = response as HttpServletResponse

        def actualUri =  httpServletRequest.requestURI - httpServletRequest.contextPath

        logger.debug "Actual URI is ${actualUri}; endpoint URL is ${endpointUrl}"

        //Only apply filter to the configured URL
        if (!active || (actualUri != endpointUrl)) {
            chain.doFilter(request, response)
            return
        }

        logger.debug "Applying authentication filter to this request"

        //Only POST is supported
        if (httpServletRequest.method != 'POST') {
            logger.debug "${httpServletRequest.method} HTTP method is not supported. Setting status to ${HttpServletResponse.SC_METHOD_NOT_ALLOWED}"
            httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
            return
        }

        String principal, credentials
        if (["application/json", "text/json"].any { httpServletRequest.contentType.contains(it) }) {
            // Prevent error if there is no json body
            def text = httpServletRequest.reader.text
            if (text) {
              try {
                def json = new JsonSlurper().parseText(text)
                principal = json[usernameField]
                credentials = json[passwordField]
              } catch (JsonException e){
                principal = null
                credentials = null
              }
            }
        } else {
            principal = request.getParameter(usernameField)
            credentials = request.getParameter(passwordField)
        }

         //Request must contain parameters
        if (!principal || !credentials) {
            logger.debug "Username and/or password parameters are missing. Setting status to ${HttpServletResponse.SC_BAD_REQUEST}"
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST) //400
            return
        }

        UsernamePasswordAuthenticationToken authenticationRequest = new UsernamePasswordAuthenticationToken(principal, credentials)

        authenticationRequest.details = authenticationDetailsSource.buildDetails(httpServletRequest)

        try {
            logger.debug "Trying to authenticate the request"
            def authenticationResult = authenticationManager.authenticate(authenticationRequest)

            if (authenticationResult.authenticated) {
                logger.debug "Request authenticated. Storing the authentication result in the security context"
                logger.debug "Authentication result: ${authenticationResult}"

                String salt = userSaltProvider.getUserSalt(principal)

                // When the config `newTokenInvalidate` is active we invalidate the previous token
                // by updating the user's salt
                if (shouldInvalidateAfterNewToken) {
                    salt = UUID.randomUUID().toString() // New random salt
                    userSaltProvider.updateUserSalt(principal, salt)
                }

                String tokenValue = statelessTokenProvider.generateToken(principal, salt, [:])
                logger.debug "Generated token: ${tokenValue}"

                httpServletResponse.setContentType("application/json")
                httpServletResponse.setStatus(HttpServletResponse.SC_CREATED)
                httpServletResponse.writer << ([token: "$tokenValue"] as JSON).toString()
            }
        } catch (AuthenticationException ae) {
            logger.debug "Authentication failed: ${ae.message}"
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED) //401
        }
    }
}
