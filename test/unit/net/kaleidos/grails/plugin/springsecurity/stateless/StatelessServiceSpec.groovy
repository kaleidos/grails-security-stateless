package net.kaleidos.grails.plugin.springsecurity.stateless

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
        CH.config.grails.plugin.springsecurity.stateless.secretKey = "secret"
    }

    void "generate a key"() {
        setup:
            def data = [id:'1', user:'palba', expirationDate:'01/01/2015']
        when:
            def token = StatelessService.generateToken(data)
        then:
            token == "eyJpZCI6IjEiLCJ1c2VyIjoicGFsYmEiLCJleHBpcmF0aW9uRGF0ZSI6IjAxLzAxLzIwMTUifV8vd3k4US9rSFlGdXYrWnJvb1AvWUliblpQOVVGMzFtMkdLSk4weFhmSjFrPQ=="
    }


    void "validate a key"() {
        setup:
            def token = "eyJpZCI6IjEiLCJ1c2VyIjoicGFsYmEiLCJleHBpcmF0aW9uRGF0ZSI6IjAxLzAxLzIwMTUifV8vd3k4US9rSFlGdXYrWnJvb1AvWUliblpQOVVGMzFtMkdLSk4weFhmSjFrPQ=="
        when:
            def data = StatelessService.validateAndExtractToken(token)
        then:
            data.id == '1'
            data.user == 'palba'
            data.expirationDate == '01/01/2015'
    }


}
