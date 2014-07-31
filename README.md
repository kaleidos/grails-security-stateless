grails-security-stateless
=========================

  Grails plugin to implement *really* stateless authentication. It is based on the procedure described at http://www.kaleidos.net/blog/295/stateless-authentication-with-api-rest/
  
   Long story short: we do not keep tokens. We generate a self-contained token, sign it, and give it to the client. The client then send this token on every request. As the token is signed, the client can't mess with it. And as the token is self-contained, we don't need to keep it, only verify it.
   
   
   
   In general, when you try to access a protected method, you will get a 401 if there isn't a valid token on the Authorization header.


Standalone
----------

The plugin can be used on a pure stand alone mode. For this, you only need to set two parameters on Config.groovy:

```
grails.plugin.security.stateless.secretKey = "mysupersecretkey"
grails.plugin.springsecurity.active = false
```

Then, you can mark any controller or method with @SecuredStateless, and the plugin will keep the authentication for you, adding to the request an attribute 'securityStatelessMap' with the user data.

```
import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless

class HelloController {
    @SecuredStateless
    def index() {
        render "hello ${request.securityStatelessMap.username}"
    }
}
```


Integrated with springsecurity
------------------------------

The plugin can be used along with springsecurity. In order to do so, you need to set several parameters on Config.groovy, including a chainmap for the urls on which you want to use the stateless authentication

```
grails.plugin.security.stateless.secretKey = "mysupersecretkey"
grails.plugin.security.stateless.springsecurity.integration = true
grails.plugin.springsecurity.filterChain.chainMap = [
    '/hello/*': 'statelessAuthenticationFilter'
]
```

Then,  any method controlled by the defined chainMap is secured. And inside it, you can access both springSecurityService.currentUser and request.securityStatelessMap.


```
import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless
import grails.plugin.springsecurity.annotation.Secured


class HelloController {
    def springSecurityService

    def index() {
        render "hello ${springSecurityService.currentUser.username} ${request.securityStatelessMap.username}"
    }
}
```
