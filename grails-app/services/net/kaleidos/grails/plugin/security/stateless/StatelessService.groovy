package net.kaleidos.grails.plugin.security.stateless

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import java.security.InvalidKeyException

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@CompileStatic
class StatelessService {

    static transactional = false

    private static final String BEARER = "Bearer "

    private String secret
    private boolean cypher

    CryptoService cryptoService

    void init(secret, cypher) {
        this.secret = secret
        this.cypher = cypher
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
        String text = new JsonBuilder(data).toString()

        if (cypher) {
            text = cryptoService.encrypt(text)
        }

        def hash = hmacSha256(text)
        String extendedData = text+"_"+hash
        return extendedData.getBytes("UTF-8").encodeBase64()
    }

    Map validateAndExtractToken(String token){
        try {
            if (token.startsWith(BEARER)){
                token = token.substring(BEARER.size())
            }

            String data = new String(token.decodeBase64())

            def split = data.split("_")

            def slurper = new JsonSlurper()
            def hash1 = split[1]
            def hash2 = hmacSha256(split[0])

            if (hash1 == hash2) {
                def text = split[0]
                if (cypher) {
                    text = cryptoService.decrypt(text)
                }
                return (Map)slurper.parseText(text)
            }

        } catch (e){
            //do nothing
            //e.printStackTrace()
        }
        return [:]
    }
}
