package net.kaleidos.grails.plugin.security.stateless.filter

import grails.test.mixin.TestFor
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.FilterChain

import net.kaleidos.grails.plugin.security.stateless.token.StatelessTokenProvider
import net.kaleidos.grails.plugin.security.stateless.provider.UserSaltProvider

import org.springframework.security.authentication.AuthenticationDetailsSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication

import grails.test.mixin.web.ControllerUnitTestMixin
import grails.test.mixin.TestMixin

@TestMixin(ControllerUnitTestMixin)
class StatelessLoginFilterSpec extends Specification {
    void "Login success (json input)"() {
        given: "The filter"
            def filter = new StatelessLoginFilter (
                active: true,
                endpointUrl: "/stateless/login",
                usernameField: "theUsername",
                passwordField: "thePassword",
            )

        and: "The authentication details source"
            filter.authenticationDetailsSource = Stub(AuthenticationDetailsSource) {
                buildDetails(_) >> [:]
            }

            filter.authenticationManager = Mock(AuthenticationManager)
            filter.userSaltProvider = Mock(UserSaltProvider)
            filter.statelessTokenProvider = Mock(StatelessTokenProvider)

        and: "The input of the request"
            def request = Stub(HttpServletRequest) {
                getRequestURI() >> "https://fakeurl.com/myapp/stateless/login"
                getContextPath() >> "https://fakeurl.com/myapp"
                getMethod() >> "POST"
                getContentType() >> "application/json"
                getReader() >> new BufferedReader(new StringReader("""
                {
                    "theUsername" : "myuser",
                    "thePassword" : "mysecret"
                }
                """))
            }
            def response = Stub(HttpServletResponse) {
                getWriter() >> new PrintWriter(System.out)
            }
            def chain = Mock(FilterChain)

        when: "we call the filter"
            filter.doFilter(request, response, chain)

        then: "the filter retrieves an error"
            0 * chain.doFilter(request, response)
            1 * filter.statelessTokenProvider.generateToken(_, _, _) >> "token"
            1 * filter.authenticationManager.authenticate(_) >> Stub(Authentication) {
                isAuthenticated() >> true
            }
            1 * filter.userSaltProvider.getUserSalt(_) >> "salt"
            0 * _._
    }

    void "Login success with user invalidation (json input)"() {
        given: "The filter"
            def filter = new StatelessLoginFilter (
                active: true,
                endpointUrl: "/stateless/login",
                usernameField: "theUsername",
                passwordField: "thePassword",
                shouldInvalidateAfterNewToken: true
            )

        and: "The authentication details source"
            filter.authenticationDetailsSource = Stub(AuthenticationDetailsSource) {
                buildDetails(_) >> [:]
            }

            filter.authenticationManager = Mock(AuthenticationManager)
            filter.userSaltProvider = Mock(UserSaltProvider)
            filter.statelessTokenProvider = Mock(StatelessTokenProvider)

        and: "The input of the request"
            def request = Stub(HttpServletRequest) {
                getRequestURI() >> "https://fakeurl.com/myapp/stateless/login"
                getContextPath() >> "https://fakeurl.com/myapp"
                getMethod() >> "POST"
                getContentType() >> "application/json"
                getReader() >> new BufferedReader(new StringReader("""
                {
                    "theUsername" : "myuser",
                    "thePassword" : "mysecret"
                }
                """))
            }
            def response = Stub(HttpServletResponse) {
                getWriter() >> new PrintWriter(System.out)
            }
            def chain = Mock(FilterChain)

        when: "we call the filter"
            filter.doFilter(request, response, chain)

        then: "the filter retrieves an error"
            0 * chain.doFilter(request, response)
            1 * filter.statelessTokenProvider.generateToken(_, _, _) >> "token"
            1 * filter.authenticationManager.authenticate(_) >> Stub(Authentication) {
                isAuthenticated() >> true
            }
            1 * filter.userSaltProvider.updateUserSalt("myuser", _)
            1 * filter.userSaltProvider.getUserSalt(_) >> "salt"
            0 * _._
    }

    void "The filter is not active"() {
        given: "The filter"
            def filter = new StatelessLoginFilter(
                endpointUrl: "/stateless/login"
            )

        and: "The input of the request"
            def request = Mock(HttpServletRequest) {
                getRequestURI() >> "https://fakeurl.com/myapp/stateless/login"
                getContextPath() >> "https://fakeurl.com/myapp"
            }
            def response = Mock(HttpServletResponse)
            def chain = Mock(FilterChain)

        and: "is not active"
            filter.active = false

        when: "we call the filter"
            filter.doFilter(request, response, chain)

        then: "the filter just pass through"
            1 * chain.doFilter(request, response)
    }

    void "Is calling to the login with a method different from POST"() {
        given: "The filter"
            def filter = new StatelessLoginFilter
            (
             active: true,
             endpointUrl: "/stateless/login"
            )

        and: "The input of the request"
            def request = Mock(HttpServletRequest) {
                getRequestURI() >> "https://fakeurl.com/myapp/stateless/login"
                getContextPath() >> "https://fakeurl.com/myapp"
                getMethod() >> requestMethod
            }
            def response = Mock(HttpServletResponse)
            def chain = Mock(FilterChain)

        when: "we call the filter"
            filter.doFilter(request, response, chain)

        then: "the filter retrieves an error"
            0 * chain.doFilter(request, response)
            1 * response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED)

        where:
            requestMethod << ["GET", "OPTIONS", "DELETE", "HEAD"]
    }

}
