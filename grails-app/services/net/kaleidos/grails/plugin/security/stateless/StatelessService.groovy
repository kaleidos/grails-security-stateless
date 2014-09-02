package net.kaleidos.grails.plugin.security.stateless

import grails.util.Holders as CH


import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.InvalidKeyException
import javax.crypto.SecretKey
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class StatelessService {
    private static String BEARER = "Bearer "

    private static String getSecret(){
        return CH.config.grails.plugin.security.stateless.secretKey
    }


    private static String hmacSha256(String data) {
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

    static String generateToken(String userName, Map<String,String> extraData=[:]){
        def data = [username:userName, extradata: extraData]
        def jsonString = new JsonBuilder(data).toString()
        def hash = hmacSha256(jsonString)
        def extendedData = jsonString+"_"+hash
        return BEARER + (extendedData as String).getBytes("UTF-8").encodeBase64()
    }


    static Map validateAndExtractToken(String token){
        try {
            if (token.startsWith(BEARER)){
                token = token.substring(BEARER.size())
            }

            String data = new String((token.decodeBase64()))
            def split = data.split("_")
            def slurper = new JsonSlurper()
            def json = slurper.parseText(split[0])
            def hash1 = split[1]
            def hash2 = hmacSha256(split[0])

            if (hash1 == hash2) {
                return slurper.parseText(split[0])
            }

        } catch (Exception e){
            //do nothing
            //e.printStackTrace()
        }
        return [:]

    }

}
