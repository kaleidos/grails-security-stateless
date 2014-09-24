package net.kaleidos.grails.plugin.security.stateless.token

import grails.util .Holders

import groovy.transform.CompileStatic

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.security.core.userdetails.UserDetails

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

import net.kaleidos.grails.plugin.security.stateless.provider.UserSaltProvider

@CompileStatic
public class StatelessTokenValidator {
    UserSaltProvider userSaltProvider

    Integer expirationTime

    public void init(Integer expirationTime) {
        this.expirationTime = expirationTime
    }

    public boolean validate(Map securityStatelessMap, UserDetails userDetails) {
        boolean validationResult = true

        if (!userDetails.enabled || !userDetails.accountNonExpired || !userDetails.credentialsNonExpired || !userDetails.accountNonLocked){
            validationResult = false
        }

        if (validationResult && expirationTime != null && securityStatelessMap["issued_at"]) {
            def formatter = ISODateTimeFormat.dateTime()
            def issuedAt = formatter.parseDateTime(""+securityStatelessMap["issued_at"])
            validationResult = issuedAt.plusMinutes(expirationTime).isAfterNow()
        }

        if (validationResult && userSaltProvider) {
            String userSalt = userSaltProvider.getUserSalt(userDetails.username)

            if (!userSalt && !securityStatelessMap["salt"]) {
                validationResult = true
            } else if (userSalt != securityStatelessMap["salt"]) {
                validationResult = false
            }
        }
        return validationResult
    }
}
