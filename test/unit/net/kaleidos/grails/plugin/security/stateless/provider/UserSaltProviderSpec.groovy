package net.kaleidos.grails.plugin.security.stateless.provider

import spock.lang.Specification

import grails.test.mixin.TestMixin
import grails.test.mixin.gorm.Domain
import grails.test.mixin.hibernate.HibernateTestMixin

import test.TestUser

@Domain(TestUser)
@TestMixin(HibernateTestMixin)
class UserSaltProviderSpec extends Specification {
    void "Retrieve user salt"() {
        setup:
            def provider = new UserSaltProvider()
            provider.init(TestUser, "username", "salt")

            new TestUser(username: username, salt: salt).save(flush:true)

        when:
            def result = provider.getUserSalt(username)

        then:
            result == salt

        where:
            username = "test1"
            salt = "salt"
    }

    void "Retrieve user salt (user doesn't exist')"() {
        setup:
            def provider = new UserSaltProvider()
            provider.init(TestUser, "username", "salt")

        when:
            def result = provider.getUserSalt(username)

        then:
            result == null

        where:
            username = "test2"
            salt = "salt"
    }

    void "Update user salt"() {
        setup:
            def provider = new UserSaltProvider()
            provider.init(TestUser, "username", "salt")

            new TestUser(username: username, salt: salt).save()

        when:
            provider.updateUserSalt(username, newSalt)
            def result = TestUser.findByUsername(username)

        then:
            result.salt == newSalt

        where:
            username = "test3"
            salt = "salt"
            newSalt = "newSalt"
    }
}
