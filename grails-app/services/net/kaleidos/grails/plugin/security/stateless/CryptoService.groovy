package net.kaleidos.grails.plugin.security.stateless

import grails.util.Holders as CH

import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.HashMap

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec



class CryptoService {
    private static String getSecret(){
        return CH.config.grails.plugin.security.stateless.secretKey
    }

    static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG")
        byte[] salt = new byte[16]
        sr.nextBytes(salt)
        return salt
    }

    private static SecretKeySpec generateKey(byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1")
        KeySpec spec = new PBEKeySpec(getSecret().toCharArray(), salt, 20000,
                128)
        SecretKey tmp = factory.generateSecret(spec)
        SecretKeySpec key = new SecretKeySpec(tmp.getEncoded(), "AES")

        return key
    }


    private static HashMap<String, byte[]> _encrypt(String text) throws Exception {
        byte[] salt = getSalt()
        SecretKeySpec secret = generateKey(salt)
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        cipher.init(Cipher.ENCRYPT_MODE, secret)

        byte[] iv = cipher.getParameters()
                .getParameterSpec(IvParameterSpec.class).getIV()
        byte[] ciphertext = cipher.doFinal(text.getBytes("UTF-8"))

        HashMap<String, byte[]> m = new HashMap<String, byte[]>()
        m.put("ciphertext", ciphertext)
        m.put("iv", iv)
        m.put("salt", salt)

        return m
    }


    private static String _decrypt(byte[] text, byte[] iv, byte[] salt)
            throws Exception {
        SecretKeySpec secret = generateKey(salt)
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv))
        byte[] decValue = cipher.doFinal(text)
        return new String(decValue)
    }


    public static String encrypt(String text){
        try {
            HashMap<String, byte[]> map = _encrypt(text)
            String encripted= "${map.ciphertext.encodeHex().toString()}${map.iv.encodeHex().toString()}${map.salt.encodeHex().toString()}"
            return encripted

        } catch (Exception e){
            e.printStackTrace()
            return null
        }
    }

    public static String decrypt(String cypherText){
        try {
            byte[] salt = cypherText[-32..-1].decodeHex()
            byte[] iv = cypherText[-64..-33].decodeHex()
            byte[] text = cypherText[0..-65].decodeHex()

            String decripted = _decrypt(text, iv, salt)

            return decripted

        } catch (Exception e){
            return null
        }
    }



}
