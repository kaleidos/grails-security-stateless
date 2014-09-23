package net.kaleidos.grails.plugin.security.stateless

import grails.test.mixin.TestFor
import spock.lang.Specification

import net.kaleidos.grails.plugin.security.stateless.provider.UserSaltProvider


@TestFor(StatelessService)
class StatelessServiceSpec extends Specification {

    def setup() {
        service.init "secret"
        service.cryptoService = new CryptoService()
        service.cryptoService.init 'secret'
        service.userSaltProvider = Mock(UserSaltProvider)
        service.userSaltProvider.getUserSalt(_) >> "salt"
    }

    void "generate a token and then extract it"() {
        setup:
            def username = 'palba'
            def token = service.generateToken(username)

        when:
            def data = service.validateAndExtractToken(token)

        then:
            data.username == 'palba'
            data.extradata == [:]
            data.issued_at != null
            data.salt == "salt"
    }

    void "generate a token with extra data"() {
        setup:
            def username = 'palba'
            def extraData = ['token1':'AAA', 'token2':'BBB']
            def token = service.generateToken(username, extraData)

        when:
            def data = service.validateAndExtractToken(token)

        then:
            data.username == 'palba'
            data.extradata['token1'] == 'AAA'
            data.extradata['token2'] == 'BBB'
            data.issued_at != null
            data.salt == "salt"
    }

    void "Try to extract invalid token"() {
        setup:
            def token = "XXXXXXXXXXXX"

        when:
            def data = service.validateAndExtractToken(token)

        then:
            thrown(RuntimeException)
    }
}
