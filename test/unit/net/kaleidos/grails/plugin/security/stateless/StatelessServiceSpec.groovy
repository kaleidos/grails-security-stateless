package net.kaleidos.grails.plugin.security.stateless

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin

import spock.lang.Specification
import spock.lang.Unroll
import spock.lang.Shared

import grails.util.Holders as CH


@TestFor(StatelessService)
class StatelessServiceSpec extends Specification {

    def setup() {
        CH.config.grails.plugin.security.stateless.secretKey = "secret"
    }

    void "generate a key"() {
        setup:
            def username = 'palba'
        when:
            def token = StatelessService.generateToken(username)
        then:
            token == "Bearer eyJ1c2VybmFtZSI6InBhbGJhIiwiZXh0cmFkYXRhIjp7fX1fenBMTklBa0o0MXpNVVZnUm9kVGlta1FueEM3U0ZVMGZkaGJUaUZWOFpPST0="
    }


    void "validate a key"() {
        setup:
            def token = "Bearer eyJ1c2VybmFtZSI6InBhbGJhIiwiZXh0cmFkYXRhIjp7fX1fenBMTklBa0o0MXpNVVZnUm9kVGlta1FueEM3U0ZVMGZkaGJUaUZWOFpPST0="
        when:
            def data = StatelessService.validateAndExtractToken(token)
        then:
            data.username == 'palba'
            data.extradata == [:]
    }

    void "generate a key with extra data"() {
        setup:
            def username = 'palba'
            def extraData = ['token1':'AAA', 'token2':'BBB']
        when:
            def token = StatelessService.generateToken(username, extraData)
        then:
            token == "Bearer eyJ1c2VybmFtZSI6InBhbGJhIiwiZXh0cmFkYXRhIjp7InRva2VuMSI6IkFBQSIsInRva2VuMiI6IkJCQiJ9fV81RDRFUzFLSzZlRHhZdDV1em5vTThKR0pzczlBMDE0bEprMS8rV1R4TVpjPQ=="
    }

    void "validate a key with extra data"() {
        setup:
            def token = "Bearer eyJ1c2VybmFtZSI6InBhbGJhIiwiZXh0cmFkYXRhIjp7InRva2VuMSI6IkFBQSIsInRva2VuMiI6IkJCQiJ9fV81RDRFUzFLSzZlRHhZdDV1em5vTThKR0pzczlBMDE0bEprMS8rV1R4TVpjPQ=="
        when:
            def data = StatelessService.validateAndExtractToken(token)
        then:
            data.username == 'palba'
            data.extradata['token1'] == 'AAA'
            data.extradata['token2'] == 'BBB'
    }


}
