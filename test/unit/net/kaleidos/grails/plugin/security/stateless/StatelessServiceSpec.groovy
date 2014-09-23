package net.kaleidos.grails.plugin.security.stateless

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(StatelessService)
class StatelessServiceSpec extends Specification {

    def setup() {
        service.init "secret", false
          service.cryptoService = new CryptoService()
          service.cryptoService.init 'secret'
    }

    void "generate a token"() {
        setup:
            def username = 'palba'
        when:
            def token = service.generateToken(username)
        then:
            token == "eyJ1c2VybmFtZSI6InBhbGJhIiwiZXh0cmFkYXRhIjp7fX1fenBMTklBa0o0MXpNVVZnUm9kVGlta1FueEM3U0ZVMGZkaGJUaUZWOFpPST0="
    }


    void "validate a token"() {
        setup:
            def token = "Bearer eyJ1c2VybmFtZSI6InBhbGJhIiwiZXh0cmFkYXRhIjp7fX1fenBMTklBa0o0MXpNVVZnUm9kVGlta1FueEM3U0ZVMGZkaGJUaUZWOFpPST0="
        when:
            def data = service.validateAndExtractToken(token)
        then:
            data.username == 'palba'
            data.extradata == [:]
    }

    void "generate a token with extra data"() {
        setup:
            def username = 'palba'
            def extraData = ['token1':'AAA', 'token2':'BBB']
        when:
            def token = service.generateToken(username, extraData)
        then:
            token == "eyJ1c2VybmFtZSI6InBhbGJhIiwiZXh0cmFkYXRhIjp7InRva2VuMSI6IkFBQSIsInRva2VuMiI6IkJCQiJ9fV81RDRFUzFLSzZlRHhZdDV1em5vTThKR0pzczlBMDE0bEprMS8rV1R4TVpjPQ=="
    }

    void "validate a token with extra data"() {
        setup:
            def token = "Bearer eyJ1c2VybmFtZSI6InBhbGJhIiwiZXh0cmFkYXRhIjp7InRva2VuMSI6IkFBQSIsInRva2VuMiI6IkJCQiJ9fV81RDRFUzFLSzZlRHhZdDV1em5vTThKR0pzczlBMDE0bEprMS8rV1R4TVpjPQ=="
        when:
            def data = service.validateAndExtractToken(token)
        then:
            data.username == 'palba'
            data.extradata['token1'] == 'AAA'
            data.extradata['token2'] == 'BBB'
    }

    void "generate an encrypted token"() {
        setup:
            service.init "secret", true
            def username = 'palba'
        when:
            def token = service.generateToken(username)
        then:
            token != "eyJ1c2VybmFtZSI6InBhbGJhIiwiZXh0cmFkYXRhIjp7fX1fenBMTklBa0o0MXpNVVZnUm9kVGlta1FueEM3U0ZVMGZkaGJUaUZWOFpPST0="
    }

    void "validate an encripted token"() {
        setup:
            service.init "secret", true
            def token = "Bearer OWY3MTU0YTZmNjI2Zjc3YzA1YzkyZGI4MDFiMDVmYzNkMTRiMjRlOTIyNWZjMDQ4ZmIxYjVmZDQ4ZTdjNWQ4MzkyMGNmM2E0MjE0ZDI1NjFjMWMzOWUyODljM2FjZmYxNTdjNWExMTAwMjVjMTRmNTJlODZhNjYxYjg5NGJkYTRlMWU5NzdkZjE2MTMwN2JiZDM2OTYwNjcwYTBkMDYxYl93NGxJbnJ0V2R2Qjk3WEhpYWhTRW5OeVpBYmJTb2UwU3hTYURkMHFxVW5nPQ=="
        when:
            def data = service.validateAndExtractToken(token)
        then:
            data.username == 'palba'
            data.extradata == [:]
    }

    void "generate and validate an encrypted token"() {
        setup:
            service.init "secret", false
            def username = 'palba'
        when:
            def token = service.generateToken(username)
            def data = service.validateAndExtractToken(token)
        then:
            data.username == 'palba'
            data.extradata == [:]
    }
}
