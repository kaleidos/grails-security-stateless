import net.kaleidos.grails.plugin.security.stateless.filter.StatelessAuthenticationFilter
import net.kaleidos.grails.plugin.security.stateless.filter.StatelessLoginFilter
import net.kaleidos.grails.plugin.security.stateless.handler.StatelessAuthenticationFailureHandler
import net.kaleidos.grails.plugin.security.stateless.provider.StatelessAuthenticationProvider


class SecurityStatelessGrailsPlugin {
    def version = "0.0.1-SNAPSHOT"
    def grailsVersion = "2.2 > *"
    def title = "Grails Spring Security Stateless Plugin"
    def description = 'Implements stateless authentication, with optional use of using Spring Security.'
    def documentation = "https://github.com/kaleidos/grails-security-stateless"
    def license = "APACHE"
    def organization = [ name: "Kaleidos Open Source SL", url: "http://kaleidos.net/" ]
    def developers = [ [ name: "Pablo Alba", email: "pablo.alba@kaleidos.net" ]]
    def scm = [ url: "https://github.com/kaleidos/grails-security-stateless" ]
    def issueManagement = [system: 'GITHUB', url: 'https://github.com/kaleidos/grails-security-stateless/issues']

    def doWithSpring = {

        def conf = application.config.grails.plugin.security.stateless.springsecurity

        if (!conf.integration) {
            return
        }

        println '\nConfiguring Spring Security Stateless ...'

        statelessAuthenticationFilter(StatelessAuthenticationFilter) {
            authenticationFailureHandler = ref('statelessAuthenticationFailureHandler')
            statelessAuthenticationProvider = ref('statelessAuthenticationProvider')
            active = true
        }

        statelessAuthenticationProvider(StatelessAuthenticationProvider) {
            userDetailsService = ref('userDetailsService')
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
    }
    
    def doWithApplicationContext = { ctx ->

        def conf = ctx.grailsApplication.config.grails.plugin.security.stateless
    
        ctx.statelessService.init(conf.secretKey, conf.cypher?true:false)

        ctx.cryptoService.init conf.secretKey
    }
}
