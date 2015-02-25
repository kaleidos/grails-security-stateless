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

@Slf4j
class JwtStatelessTokenProvider implements StatelessTokenProvider {
    private static final String BEARER = "Bearer "

    CryptoService cryptoService

    String generateToken(String userName, String salt=null, Map<String,String> extraData=[:]){
        def data = [username:userName]

        if (extraData) {
            data["extradata"] = extraData
        }

        if (salt != null) {
            data["salt"] = salt
        }

        DateTimeFormatter formatter = ISODateTimeFormat.dateTime()
        data["issued_at"] = formatter.print(new DateTime())

        String header = new JsonBuilder([alg:"HS256", typ: "JWT"])
        String payload = new JsonBuilder(data).toString()
        String signature = cryptoService.hash("${header.bytes.encodeBase64()}.${payload.bytes.encodeBase64()}")

        return "${header.bytes.encodeBase64()}.${payload.bytes.encodeBase64()}.${signature}"
    }

    Map validateAndExtractToken(String token) {
        if (token.startsWith(BEARER)){
            token = token.substring(BEARER.size())
        }

        def (header64, payload64, signature) = token.tokenize(".")

        if (header64 == null || payload64 == null || signature == null) {
            log.debug "Token should have two points to split"
            throw new StatelessValidationException("Invalid token")
        }

        // Validate signature
        String expectedSignature = cryptoService.hash("${header64}.${payload64}")

        if (signature != expectedSignature) {
            throw new StatelessValidationException("Invalid token")
        }

        // Extract the payload
        def slurper = new JsonSlurper()
        def payload = new String(payload64.decodeBase64())

        return (Map)slurper.parseText(payload)
    }
}
