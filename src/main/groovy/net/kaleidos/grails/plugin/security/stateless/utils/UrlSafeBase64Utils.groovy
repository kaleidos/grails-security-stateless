package net.kaleidos.grails.plugin.security.stateless.utils

import net.kaleidos.grails.plugin.security.stateless.exception.StatelessValidationException

class UrlSafeBase64Utils {

    private static String replacePairs(String s, List charPairs) {
        charPairs.each { pair ->
            s = s.replaceAll(pair.first(), pair.last())
        }
        return s
    }

    static String encode(byte[] arg) {
        List charPairs = [['=', ''], ['\\+', '-'], ['/', '_']]
        String s = arg.encodeBase64()
        return replacePairs(s, charPairs)
    }

    static byte[] decode(String arg) {
        List charPairs = [['-', '+'], ['_', '/']]
        String s = replacePairs(arg, charPairs)
        switch(s.size() % 4) {
            case 0:
                break
            case 2:
                s += '=='
                break
            case 3:
                s += '='
                break
            default:
                throw new StatelessValidationException("Illegal base64url string")
        }

        return s.decodeBase64()
    }

}
