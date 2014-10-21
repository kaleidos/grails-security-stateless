import net.kaleidos.grails.plugin.security.stateless.StatelessService
import net.kaleidos.grails.plugin.security.stateless.CryptoService

import net.kaleidos.grails.plugin.security.stateless.filter.StatelessAuthenticationFilter
import net.kaleidos.grails.plugin.security.stateless.filter.StatelessLoginFilter
import net.kaleidos.grails.plugin.security.stateless.filter.StatelessInvalidateTokenFilter

import net.kaleidos.grails.plugin.security.stateless.handler.StatelessAuthenticationFailureHandler

import net.kaleidos.grails.plugin.security.stateless.provider.StatelessAuthenticationProvider
import net.kaleidos.grails.plugin.security.stateless.provider.UserSaltProvider

import net.kaleidos.grails.plugin.security.stateless.token.StatelessTokenValidator

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.SecurityFilterPosition

class SecurityStatelessGrailsPlugin {
    def version = "0.0.3-SNAPSHOT"
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
        def conf = application.config.grails.plugin.security.stateless.springsecurity

        if (!conf.integration) {
            return
        }

        println '\nConfiguring Spring Security Stateless ...'

        userSaltProvider(UserSaltProvider)
        cryptoService(CryptoService)

        statelessService(StatelessService) {
            userSaltProvider = ref("userSaltProvider")
            cryptoService = ref("cryptoService")
        }

        statelessTokenValidator(StatelessTokenValidator){
            userSaltProvider = ref("userSaltProvider")
        }

        statelessAuthenticationFilter(StatelessAuthenticationFilter) {
            authenticationFailureHandler = ref('statelessAuthenticationFailureHandler')
            statelessAuthenticationProvider = ref('statelessAuthenticationProvider')
            active = true
        }

        statelessAuthenticationProvider(StatelessAuthenticationProvider) {
            userDetailsService = ref('userDetailsService')
            statelessService = ref('statelessService')
            statelessTokenValidator = ref('statelessTokenValidator')
        }

        statelessAuthenticationFailureHandler(StatelessAuthenticationFailureHandler)

        statelessLoginFilter(StatelessLoginFilter) {
            authenticationManager = ref('authenticationManager')
            authenticationDetailsSource = ref('authenticationDetailsSource')
            statelessService = ref('statelessService')
            endpointUrl = conf.login.endpointUrl
            usernameField = conf.login.usernameField?:"user"
            passwordField = conf.login.passwordField?:"password"
            active = conf.login.active?:false
        }

        statelessInvalidateTokenFilter(StatelessInvalidateTokenFilter) {
            statelessService = ref('statelessService')
            endpointUrl = conf.invalidate.endpointUrl
            active = conf.invalidate.active?:false
        }

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

        ctx.statelessService.init "${conf.secretKey}"
        ctx.cryptoService.init "${conf.secretKey}"

        def securityConf = SpringSecurityUtils.securityConfig
        String userClassName = securityConf.userLookup.userDomainClassName

        if (userClassName) {
            def userClass = ctx.grailsApplication.getDomainClass(userClassName).clazz
            def usernamePropertyName = securityConf.userLookup.usernamePropertyName

            if (conf.expirationTime) {
                ctx.statelessTokenValidator.init(new Integer(conf.expirationTime))
            }

            ctx.userSaltProvider?.init(userClass, usernamePropertyName, conf.saltField?:"salt")
        }
    }
}
