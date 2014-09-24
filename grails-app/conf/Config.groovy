// Only for testing purpouses
grails.plugin.springsecurity.userLookup.userDomainClassName = 'test.TestUser'
grails.plugin.security.stateless.secretKey = "mysecretkey"

log4j = {
    error  'org.codehaus.groovy.grails',
           'org.springframework',
           'org.hibernate'
}
