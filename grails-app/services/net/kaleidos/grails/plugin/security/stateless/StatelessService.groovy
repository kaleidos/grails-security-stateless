package net.kaleidos.grails.plugin.security.stateless

import net.kaleidos.grails.plugin.security.stateless.provider.UserSaltProvider

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import java.security.InvalidKeyException

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat


@CompileStatic
class StatelessService {
    static transactional = false

    private static final String TOKEN_SIGNING_SEPARATOR = '$$'
    private static final String TOKEN_SIGNING_SEPARATOR_REGEX = '\\$\\$'
    private static final String BEARER = "Bearer "

    private String secret

    protected final Logger log = LoggerFactory.getLogger(getClass().name)

    CryptoService cryptoService
    UserSaltProvider userSaltProvider

    void init(String secret) {
        this.secret = secret
    }

    private String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256")
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256")
            mac.init(secretKeySpec)
            byte[] digest = mac.doFinal(data.getBytes("UTF-8"))
            return digest.encodeBase64().toString()
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception while converting to HMac SHA256")
        }
    }

    String generateToken(String userName, Map<String,String> extraData=[:]){
        def data = [username:userName, extradata: extraData]

        String userSalt = userSaltProvider.getUserSalt(userName)
        if (userSalt) {
            data["salt"] = userSalt
        }

        DateTimeFormatter formatter = ISODateTimeFormat.dateTime()
        data["issued_at"] = formatter.print(new DateTime())

        String text = new JsonBuilder(data).toString()
        text = cryptoService.encrypt(text)

        def hash = hmacSha256(text)
        String extendedData = "${text}${TOKEN_SIGNING_SEPARATOR}${hash}"
        return extendedData.getBytes("UTF-8").encodeBase64()
    }

    Map validateAndExtractToken(String token){
        if (token.startsWith(BEARER)){
            token = token.substring(BEARER.size())
        }

        String data = new String(token.decodeBase64())
        def split = data.split(TOKEN_SIGNING_SEPARATOR_REGEX)

        if (split.size() != 2) {
            throw new RuntimeException("Invalid token")
        }

        def slurper = new JsonSlurper()
        def hash1 = split[1]
        def hash2 = hmacSha256(split[0])

        if (hash1 == hash2) {
            def text = cryptoService.decrypt(split[0])
            return (Map)slurper.parseText(text)
        } else {
            throw new RuntimeException("Invalid token")
        }
    }

    void updateUserSalt(String username){
        userSaltProvider.updateUserSalt(username, UUID.randomUUID().toString())
    }

}
