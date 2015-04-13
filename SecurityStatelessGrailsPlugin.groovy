import net.kaleidos.grails.plugin.security.stateless.CryptoService

import net.kaleidos.grails.plugin.security.stateless.filter.StatelessAuthenticationFilter
import net.kaleidos.grails.plugin.security.stateless.filter.StatelessLoginFilter
import net.kaleidos.grails.plugin.security.stateless.filter.StatelessInvalidateTokenFilter

import net.kaleidos.grails.plugin.security.stateless.handler.StatelessAuthenticationFailureHandler

import net.kaleidos.grails.plugin.security.stateless.provider.StatelessAuthenticationProvider
import net.kaleidos.grails.plugin.security.stateless.provider.UserSaltProvider

import net.kaleidos.grails.plugin.security.stateless.token.StatelessTokenValidator
import net.kaleidos.grails.plugin.security.stateless.token.LegacyStatelessTokenProvider
import net.kaleidos.grails.plugin.security.stateless.token.JwtStatelessTokenProvider
import net.kaleidos.grails.plugin.security.stateless.ForbiddenEntryPoint
import net.kaleidos.grails.plugin.security.stateless.JsonDeniedHandler

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.SecurityFilterPosition

class SecurityStatelessGrailsPlugin {
    def version = "0.0.9"
    def grailsVersion = "2.2 > *"
    def title = "Grails Spring Security Stateless Plugin"
    def description = 'Implements stateless authentication, with optional use of using Spring Security.'
    def documentation = "https://github.com/kaleidos/grails-security-stateless"
    def license = "APACHE"
    def organization = [ name: "Kaleidos Open Source SL", url: "http://kaleidos.net/" ]
    def developers = [ [ name: "Pablo Alba", email: "pablo.alba@kaleidos.net" ]]
    def scm = [ url: "https://github.com/kaleidos/grails-security-stateless" ]
    def issueManagement = [system: 'GITHUB', url: 'https://github.com/kaleidos/grails-security-stateless/issues']

    def loadAfter = ["springSecurityCore"]

    def pluginExcludes = [
        "grails-app/views/error.gsp",
        "grails-app/controllers/**",
        "grails-app/domain/**",
        "grails-app/i18n/**",
        "grails-app/taglib/**",
        "grails-app/utils/**",
        "grails-app/views/**",
        "web-app/**"
    ]

    def doWithSpring = {
        // This beans could be used without Spring Security integration
        cryptoService(CryptoService)
        userSaltProvider(UserSaltProvider)

        statelessTokenValidator(StatelessTokenValidator){
            userSaltProvider = ref("userSaltProvider")
        }

        def conf = application.config.grails.plugin.security.stateless

        if (conf?.format == "JWT") {
            // JWT format
            statelessTokenProvider(JwtStatelessTokenProvider) {
                cryptoService = ref("cryptoService")
            }
        } else if (!conf?.format || conf?.format == "Legacy"){
            // Legacy format
            statelessTokenProvider(LegacyStatelessTokenProvider) {
                cryptoService = ref("cryptoService")
            }
        } else {
            throw new RuntimeException("Format ${conf?.format} is not a valid format. Allowed values: 'Legacy' or 'JWT'")
        }

        def securityConfig = SpringSecurityUtils.securityConfig
        if (!securityConfig || !securityConfig.active) {
            log.debug "Spring security not active"
            return
        }

        if (!conf?.springsecurity || !conf?.springsecurity.integration) {
            log.debug "Spring security integration disabled"
            return
        }

        println '\nConfiguring Spring Security Stateless ...'

        statelessAuthenticationFilter(StatelessAuthenticationFilter) {
            authenticationFailureHandler = ref('statelessAuthenticationFailureHandler')
            statelessAuthenticationProvider = ref('statelessAuthenticationProvider')
            accessDeniedHandler = ref('accessDeniedHandler')
            active = true
        }

        statelessAuthenticationProvider(StatelessAuthenticationProvider) {
            userDetailsService = ref('userDetailsService')
            statelessTokenProvider = ref('statelessTokenProvider')
            statelessTokenValidator = ref('statelessTokenValidator')
        }

        statelessAuthenticationFailureHandler(StatelessAuthenticationFailureHandler)

        statelessLoginFilter(StatelessLoginFilter) {
            statelessTokenProvider = ref('statelessTokenProvider')
            userSaltProvider = ref('userSaltProvider')
            authenticationManager = ref('authenticationManager')
            authenticationDetailsSource = ref('authenticationDetailsSource')
            endpointUrl = conf.springsecurity.login.endpointUrl
            usernameField = conf.springsecurity.login.usernameField?:"user"
            passwordField = conf.springsecurity.login.passwordField?:"password"
            active = conf.springsecurity.login.active?:false
        }

        statelessInvalidateTokenFilter(StatelessInvalidateTokenFilter) {
            statelessTokenProvider = ref('statelessTokenProvider')
            endpointUrl = conf.springsecurity.invalidate.endpointUrl
            active = conf.springsecurity.invalidate.active?:false
        }

        // Needed in order to not send HTML redirections
        authenticationEntryPoint(ForbiddenEntryPoint)
        accessDeniedHandler(JsonDeniedHandler)

        SpringSecurityUtils.registerFilter('statelessAuthenticationFilter', SecurityFilterPosition.SECURITY_CONTEXT_FILTER.order + 10)
        SpringSecurityUtils.registerFilter('statelessLoginFilter', SecurityFilterPosition.FIRST.order + 1)
        SpringSecurityUtils.registerFilter('statelessInvalidateTokenFilter', SecurityFilterPosition.FIRST.order + 2)

        println '\n... finished configuring Spring Security Stateless'
    }

    def doWithApplicationContext = { ctx ->
        def conf = ctx.grailsApplication.config.grails.plugin.security.stateless

        if (!conf.secretKey) {
            throw new RuntimeException("Spring security stateles secretKey not configured or empty. Please, set 'grails.plugin.security.stateless.secretKey' value in your Config.groovy")
        }

        ctx.cryptoService.init "${conf.secretKey}"

        if (conf.expiresStatusCode) {
            ctx.statelessAuthenticationFailureHandler.init(conf.expiresStatusCode)
        }

        if (conf.expirationTime) {
            ctx.statelessTokenValidator.init(new Integer(conf.expirationTime))
            ctx.statelessTokenProvider.init(new Integer(conf.expirationTime))
        }

        if (!conf?.springsecurity || !conf?.springsecurity.integration) {
            log.debug "Spring security integration disabled"
            return
        }

        def securityConf = SpringSecurityUtils.securityConfig
        String userClassName = securityConf.userLookup.userDomainClassName

        if (userClassName) {
            def userClass = ctx.grailsApplication.getDomainClass(userClassName).clazz
            def usernamePropertyName = securityConf.userLookup.usernamePropertyName
            ctx.userSaltProvider?.init(userClass, usernamePropertyName, conf.springsecurity.saltField?:"salt")
        }
    }
}
