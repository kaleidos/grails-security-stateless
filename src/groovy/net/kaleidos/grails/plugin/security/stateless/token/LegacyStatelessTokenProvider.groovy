package net.kaleidos.grails.plugin.security.stateless.token

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

import net.kaleidos.grails.plugin.security.stateless.CryptoService
import net.kaleidos.grails.plugin.security.stateless.StatelessValidationException


@CompileStatic
@Slf4j
class LegacyStatelessTokenProvider implements StatelessTokenProvider {
    private static final String TOKEN_SIGNING_SEPARATOR = '$$'
    private static final String TOKEN_SIGNING_SEPARATOR_REGEX = '\\$\\$'
    private static final String BEARER = "Bearer "

    CryptoService cryptoService

    String generateToken(String userName, String salt, Map<String,String> extraData=[:]){
        def data = [username:userName, extradata: extraData]

        if (salt != null) {
            data["salt"] = salt
        }

        DateTimeFormatter formatter = ISODateTimeFormat.dateTime()
        data["issued_at"] = formatter.print(new DateTime())

        String text = new JsonBuilder(data).toString()
        text = cryptoService.encrypt(text)

        def hash = cryptoService.hash(text)
        String extendedData = "${text}${TOKEN_SIGNING_SEPARATOR}${hash}"
        return extendedData.getBytes("UTF-8").encodeBase64()
    }

    Map validateAndExtractToken(String token) {
        if (token.startsWith(BEARER)){
            token = token.substring(BEARER.size())
        }

        String data = new String(token.decodeBase64())
        def split = data.split(TOKEN_SIGNING_SEPARATOR_REGEX)

        if (split.size() != 2) {
            throw new StatelessValidationException("Invalid token")
        }

        def slurper = new JsonSlurper()
        def hash1 = split[1]
        def hash2 = cryptoService.hash(split[0])

        if (hash1 != hash2) {
            throw new StatelessValidationException("Invalid token")
        }

        def text = cryptoService.decrypt(split[0])
        def result = (Map)slurper.parseText(text)

        return result
    }
}
