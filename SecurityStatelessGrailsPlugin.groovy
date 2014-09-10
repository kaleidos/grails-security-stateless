import grails.plugin.springsecurity.SpringSecurityUtils
import net.kaleidos.grails.plugin.security.stateless.filter.StatelessAuthenticationFilter
import net.kaleidos.grails.plugin.security.stateless.filter.StatelessLoginFilter
import net.kaleidos.grails.plugin.security.stateless.provider.StatelessAuthenticationProvider
import net.kaleidos.grails.plugin.security.stateless.handler.StatelessAuthenticationFailureHandler
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.util.Holders as CH


class SecurityStatelessGrailsPlugin {
    def version = "0.0.1"
    def grailsVersion = "2.2 > *"

    def title = "Grails Spring Security Stateless Plugin"
    def author = "Pablo Alba"
    def authorEmail = "pablo.alba@kaleidos.net"
    def description = '''\
 Grails plugin to implement stateless authentication, with optional use of using Spring Security.
'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/kaleidos/grails-security-stateless"

    def license = "APACHE"

    def organization = [ name: "Kaleidos Open Source SL", url: "http://kaleidos.net/" ]

    def developers = [ [ name: "Pablo Alba", email: "pablo.alba@kaleidos.net" ]]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/kaleidos/grails-security-stateless" ]


    def doWithSpring = {

        println '\nConfiguring Spring Security Stateless ...'

        if (CH.config.grails.plugin.security.stateless.springsecurity.integration) {

            statelessAuthenticationFilter(StatelessAuthenticationFilter) {
                authenticationFailureHandler = ref('statelessAuthenticationFailureHandler')
                statelessAuthenticationProvider = ref('statelessAuthenticationProvider')
                active = true
            }


            statelessAuthenticationProvider(StatelessAuthenticationProvider) {
                userDetailsService = ref('userDetailsService')
            }


            statelessAuthenticationFailureHandler(StatelessAuthenticationFailureHandler) {
            }


            statelessLoginFilter(StatelessLoginFilter) {
                authenticationManager = ref('authenticationManager')
                authenticationDetailsSource = ref('authenticationDetailsSource')
                endpointUrl = CH.config.grails.plugin.security.stateless.springsecurity.login.endpointUrl
                usernameField = CH.config.grails.plugin.security.stateless.springsecurity.login.usernameField?:"user"
                passwordField = CH.config.grails.plugin.security.stateless.springsecurity.login.passwordField?:"password"
                active = CH.config.grails.plugin.security.stateless.springsecurity.login.active?:false
            }


        }




    }


}
