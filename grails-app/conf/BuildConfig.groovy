grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'target/docs'
grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {
    inherits "global"
    log "warn"

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compile "net.sf.ehcache:ehcache-core:2.4.8"
        compile 'joda-time:joda-time:2.4'

        // Unit testing domain gorm access
        test 'org.grails:grails-datastore-test-support:1.0-grails-2.4'

        // In order to support mock of classes
        test 'cglib:cglib-nodep:2.2'
    }

    plugins {
        compile ":spring-security-core:2.0-RC4"

        test ':hibernate4:4.3.5.4'

        build ':release:3.0.1', ':rest-client-builder:2.0.3', {
           export = false
        }
    }
}
