package net.kaleidos.grails.plugin.security.stateless.utils

import groovy.json.JsonBuilder
import spock.lang.Specification
import spock.lang.Unroll

import net.kaleidos.grails.plugin.security.stateless.exception.StatelessValidationException

class UrlSafeBase64UtilsSpec extends Specification {

    void 'Correctly encode a JSON payload'() {
        given: 'a JSON payload as a String'
            String payload = (new JsonBuilder([alg: 'H256', typ: 'JWT'])).toString()

        and: 'its known url safe base64 encoding'
            String encoding = 'eyJhbGciOiJIMjU2IiwidHlwIjoiSldUIn0'

        when: 'trying to encode the payload'
            String result = UrlSafeBase64Utils.encode(payload.bytes)

        then: 'the result should match the expected encoding'
            encoding == result
    }

    @Unroll
    void 'Correctly decode url safe base64 payload [#payload --> #expectedResult]'() {
        when: 'trying to decode the payload'
            byte[] byteResult = UrlSafeBase64Utils.decode(payload)
            String stringResult = new String(byteResult)

        then: 'the result should match the expected string'
            stringResult == expectedResult

        where: 'we test against the following cases'
            payload                  | expectedResult
            'aGVsbG8gd29ybGQ'        | 'hello world'
            'eyJoZWxsbyI6IDEyM30'    | '{"hello": 123}'
            'eyJoZWxsbyI6ICIxMjMifQ' | '{"hello": "123"}'
    }

    void 'Fail when malformed url safe base64 payload'() {
        given: 'a malformed payload'
            String payload = 'malformed'

        when: 'trying to decode it'
            UrlSafeBase64Utils.decode(payload)

        then: 'an exception should be thrown'
            thrown StatelessValidationException
    }

}
