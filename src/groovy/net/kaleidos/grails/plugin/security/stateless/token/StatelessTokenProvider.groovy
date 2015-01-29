package net.kaleidos.grails.plugin.security.stateless.token

interface StatelessTokenProvider {
    String generateToken(String userName, String salt, Map<String,String> extraData)
    Map validateAndExtractToken(String token)
}