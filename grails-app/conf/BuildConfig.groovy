grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'target/docs'
grails.project.dependency.resolver = "maven"

grails.project.fork = [
    //test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    inherits "global"
    log "warn"

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.27'
        compile "net.sf.ehcache:ehcache-core:2.4.8"

    }

    plugins {
        compile ":spring-security-core:2.0-RC4"
        build ':release:3.0.1', ':rest-client-builder:2.0.3', {
           export = false
        }
    }


}
