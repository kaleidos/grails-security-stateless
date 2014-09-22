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
    }

    plugins {
        compile ":spring-security-core:2.0-RC4"
        build ':release:3.0.1', ':rest-client-builder:2.0.3', {
           export = false
        }
    }
}
