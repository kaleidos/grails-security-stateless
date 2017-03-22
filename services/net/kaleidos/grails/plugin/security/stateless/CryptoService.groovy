package net.kaleidos.grails.plugin.security.stateless

import groovy.transform.CompileStatic

import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.InvalidKeyException
import java.security.spec.KeySpec

import javax.crypto.Mac
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import net.kaleidos.grails.plugin.security.stateless.utils.UrlSafeBase64Utils

@CompileStatic
class CryptoService {

    static transactional = false

    protected final Logger log = LoggerFactory.getLogger(getClass().name)

    private String secret

    void init(String secret) {
        this.secret = secret
    }

    byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG")
        byte[] salt = new byte[16]
        sr.nextBytes(salt)
        return salt
    }

    private SecretKeySpec generateKey(byte[] salt) {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, 20000, 128)
        SecretKey tmp = factory.generateSecret(spec)
        new SecretKeySpec(tmp.getEncoded(), "AES")
    }

    private Map<String, byte[]> _encrypt(String text) {
        byte[] salt = getSalt()
        SecretKeySpec secret = generateKey(salt)
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        cipher.init(Cipher.ENCRYPT_MODE, secret)

        byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec).getIV()
        byte[] ciphertext = cipher.doFinal(text.getBytes("UTF-8"))

        [ciphertext: ciphertext, iv: iv, salt: salt]
    }

    private String _decrypt(byte[] text, byte[] iv, byte[] salt) {
        SecretKeySpec secret = generateKey(salt)
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv))
        byte[] decValue = cipher.doFinal(text)
        return new String(decValue)
    }

    String encrypt(String text){
        try {
            Map<String, byte[]> map = _encrypt(text)
            "${map.ciphertext.encodeHex().toString()}${map.iv.encodeHex().toString()}${map.salt.encodeHex().toString()}"
        } catch (Exception e){
            log.error(e.message, e)
            null
        }
    }

    String decrypt(String cypherText){
        try {
            byte[] salt = cypherText[-32..-1].decodeHex()
            byte[] iv = cypherText[-64..-33].decodeHex()
            byte[] text = cypherText[0..-65].decodeHex()

            _decrypt(text, iv, salt)
        } catch (e){
            null
        }
    }

    String hash(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256")
            SecretKeySpec secretKeySpec = new SecretKeySpec(this.secret.getBytes("UTF-8"), "HmacSHA256")
            mac.init(secretKeySpec)
            byte[] digest = mac.doFinal(data.getBytes("UTF-8"))
            return UrlSafeBase64Utils.encode(digest)
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception while converting to HMac SHA256")
        }
    }

}
