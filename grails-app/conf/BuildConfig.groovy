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
    }

    plugins {
        //compile ":cache-ehcache:1.0.1"
        //compile ':spring-security-core:2.0-RC3'
        build ":rest-client-builder:1.0.3", { export = false }
    }
}
