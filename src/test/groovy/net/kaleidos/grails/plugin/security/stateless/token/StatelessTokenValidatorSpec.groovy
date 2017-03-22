package net.kaleidos.grails.plugin.security.stateless.token

import spock.lang.Specification
import spock.lang.Unroll

import grails.plugin.springsecurity.userdetails.GrailsUser
import net.kaleidos.grails.plugin.security.stateless.token.StatelessTokenValidator

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

import net.kaleidos.grails.plugin.security.stateless.provider.UserSaltProvider

class StatelessTokenValidatorSpec extends Specification {
    void "Validate a valid token"() {
        setup:
            def validator = new StatelessTokenValidator()

        when:
            def result = validator.validate([:], new GrailsUser(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, [], 1))

        then:
            result == true

        where:
            username = "test"
            password = "test"
            enabled = true
            accountNonExpired = true
            credentialsNonExpired = true
            accountNonLocked = true
    }

    @Unroll
    void "Not valid token: #enabled, #accountNonExpired, #credentialsNonExpired, #accountNonLocked"() {
        setup:
            def validator = new StatelessTokenValidator()

        when:
            def result = validator.validate([:], new GrailsUser(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, [], 1))

        then:
            result == false

        where:
            username = "test"
            password = "test"
            enabled << [false, true, true, true]
            accountNonExpired << [true, false, true, true]
            credentialsNonExpired << [true, true, false, true]
            accountNonLocked << [true, true, true, false]
    }

    void "Expired token"() {
        setup:
            def validator = new StatelessTokenValidator()

        and: "Setup expiration date for the validator"
            validator.init(59)

        and: "Create the issued at date inside the token"
            def formatter = ISODateTimeFormat.dateTime()
            String issuedAt = formatter.print(new DateTime().minusHours(1));

        when:
            def result = validator.validate(["issued_at" : issuedAt], new GrailsUser(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, [], 1))

        then:
            result == false

        where:
            username = "test"
            password = "test"
            enabled = true
            accountNonExpired = true
            credentialsNonExpired = true
            accountNonLocked = true
    }

    @Unroll
    void "User salt do match"() {
        setup:
            def validator = new StatelessTokenValidator()
            validator.userSaltProvider = Mock(UserSaltProvider)
            validator.userSaltProvider.getUserSalt(username) >> salt

        when:
            def result = validator.validate(["salt" : salt], new GrailsUser(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, [], 1))

        then:
            result == true

        where:
            username = "test1"
            password = "test"
            enabled = true
            accountNonExpired = true
            credentialsNonExpired = true
            accountNonLocked = true
            salt << ["salt", null]
    }

    @Unroll
    void "User salt doesn't match"() {
        setup:
            def validator = new StatelessTokenValidator()
            validator.userSaltProvider = Mock(UserSaltProvider)
            validator.userSaltProvider.getUserSalt(username) >> salt2

        when:
            def result = validator.validate(["salt" : salt1], new GrailsUser(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, [], 1))

        then:
            result == false

        where:
            username = "test2"
            password = "test"
            enabled = true
            accountNonExpired = true
            credentialsNonExpired = true
            accountNonLocked = true
            salt1 << ["salt1", null, "salt1"]
            salt2 << ["salt2", "salt2", null]
    }
}
