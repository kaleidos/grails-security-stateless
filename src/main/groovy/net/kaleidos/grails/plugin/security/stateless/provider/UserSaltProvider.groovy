package net.kaleidos.grails.plugin.security.stateless.provider

import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException;
import org.springframework.util.Assert

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UserSaltProvider {
    protected final Logger log = LoggerFactory.getLogger(getClass().name)

    Class<?> userClass
    String usernameProperty
    String saltField

    public void init(Class<?> userClass, String usernameProperty, String saltField) {
        Assert.notNull(userClass, "User class cannot be null")
        Assert.notNull(usernameProperty, "Username property field should be specified")
        Assert.notNull(saltField, "Salt property field should be specified")

        this.userClass = userClass
        this.usernameProperty = usernameProperty
        this.saltField = saltField
    }

    public String getUserSalt(String username){
        String salt = null
        userClass.withTransaction { status ->
            def user = userClass.findWhere((usernameProperty): username)

            if (!user) {
                salt = null
            } else if (userClass.metaClass.hasProperty(user, saltField)) {
                salt = user."$saltField"
            } else {
                throw new RuntimeException("$userClass class needs $saltField field")
            }
        }
        return salt
    }

    public void updateUserSalt(String username, String salt) {
        userClass.withTransaction { status ->
            def user = userClass.findWhere((usernameProperty): username)

            if (!user) {
                throw new RuntimeException("User not found")
            } else if (userClass.metaClass.hasProperty(user, saltField)) {
                user."$saltField" = salt
                user.save(failOnError: true)
            } else {
                throw new RuntimeException("$userClass class needs $saltField field")
            }
        }
    }

    public boolean isValidSalt(Map tokenData) {
        def username = tokenData["username"]
        if (username == false) {
            return false
        }

        def storedSalt = getUserSalt(username)

        if (storedSalt == null) {
            return true
        }

        return storedSalt == tokenData["salt"]
    }
}
