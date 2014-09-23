package net.kaleidos.grails.plugin.security.stateless.filter

import groovy.transform.CompileStatic

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import net.kaleidos.grails.plugin.security.stateless.StatelessService
import net.kaleidos.grails.plugin.security.stateless.provider.UserSaltProvider

import org.springframework.web.filter.GenericFilterBean

@CompileStatic
class StatelessInvalidateTokenFilter extends GenericFilterBean {
    boolean active

    String endpointUrl

    UserSaltProvider userSaltProvider
    StatelessService statelessService

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

        // Get current token
        def tokenValue = httpServletRequest.getHeader('Authorization')
        if (!tokenValue) {
            logger.debug "Authorization header not present"
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST) //400
            return
        }

        try {
            def tokenData = statelessService.validateAndExtractToken(tokenValue)
            userSaltProvider.updateUserSalt(tokenData.username as String, UUID.randomUUID().toString())
            return
        } catch (Exception e) {
            log.debug "Problem updating the user salt: ${e.message}"
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST) //400
        }
    }
}
