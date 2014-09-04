package net.kaleidos.grails.plugin.security.stateless

import grails.test.mixin.TestFor

import spock.lang.Specification
import spock.lang.Unroll
import spock.lang.Shared
import spock.lang.IgnoreRest

import javax.crypto.spec.SecretKeySpec

import grails.util.Holders as CH


@TestFor(CryptoService)
class CryptoServiceSpec extends Specification {

    def setup() {
        CH.config.grails.plugin.security.stateless.secretKey = "secret"
    }

    void "cypher a text"() {
        setup:
            def text = "Hello world, this is an encrypted message"
        when:
            def data = CryptoService.encrypt(text)
            def decripted = CryptoService.decrypt(data)
        then:
            decripted == text
    }


    void "decypher a text"() {
        setup:
            def text = "62c6e873afd3aa8458f200d559a5267161742777b611e4551aa53c4e4fa50235ff0dad809f770a8d10408186ced7d899"
        when:
            def decripted = CryptoService.decrypt(text)
        then:
            decripted == "Hello world"
    }


}
