grails-security-stateless
=========================

  Grails plugin to implement *really* stateless authentication. It is based on the procedure described at http://www.kaleidos.net/blog/295/stateless-authentication-with-api-rest/
  
   Long story short: we do not keep tokens. We generate a self-contained token, sign it, and give it to the client. The client then send this token on every request. As the token is signed, the client can't mess with it. And as the token is self-contained, we don't need to keep it, only verify it.
   
   
   
   In general, when you try to access a protected method, you will get a 401 (UNAUTHORIZED) if there isn't a valid token on the Authorization header.


Standalone
----------

#### Configuration

The plugin can be used on a pure stand alone mode. For this, you only need to set two parameters on Config.groovy:

```
grails.plugin.security.stateless.secretKey = "mysupersecretkey"
grails.plugin.springsecurity.active = false
```


#### Secure controllers and methods

You can mark any controller or method with @SecuredStateless, and the plugin will keep the authentication for you, adding to the request an attribute 'securityStatelessMap' with the user data.

```
import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless

class HelloController {
    @SecuredStateless
    def index() {
        render "hello ${request.securityStatelessMap.username}"
    }
}
```


#### Login

On standalone mode, you have to code your own login controller. This controller should validate the credentials as you need, and return a security-stateless token, using the method statelessService.generateToken(String username).

A silly example of login controller could be:

```
import grails.converters.JSON

class LoginController {
    def statelessService

    def index() {
        if (params.password == '12345'){
            render (['token': statelessService.generateToken(params.user)] as JSON)
        } else {
            render(status: 401, text: '')
        }
    }
}
```

Integrated with springsecurity
------------------------------


#### Configuration

The plugin can be used along with springsecurity. In order to do so, you need to set several parameters on Config.groovy:

```
grails.plugin.security.stateless.secretKey = "mysupersecretkey"
grails.plugin.security.stateless.springsecurity.integration = true
```

Also, you need to add the statelessAuthenticationFilter to the list of filters of your application adding this line to BootStrap.groovy

```
SpringSecurityUtils.clientRegisterFilter('statelessAuthenticationFilter', SecurityFilterPosition.SECURITY_CONTEXT_FILTER.order + 10)
```


#### Secure controllers and methods

Any method controlled by the defined chainMap is secured. And inside it, you can access both springSecurityService.currentUser and request.securityStatelessMap.


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

#### Login

You can code your own login controller. But the plugin offers you a default method for login. In order to use it, you only need to configure several parameters on Config.groovy. First, you need to add and endpoint to the chainMap. Next, define the follow parameters:

* grails.plugin.security.stateless.springsecurity.login.active: true for activate login
* grails.plugin.security.stateless.springsecurity.login.endpointUrl: the same login url defined on chainMap
* grails.plugin.security.stateless.springsecurity.login.usernameField: The name of the parameter for the username. By default it is "user".
* grails.plugin.security.stateless.springsecurity.login.passwordField: The name of the parameter for the username. By default it is "password".



```
grails.plugin.security.stateless.secretKey = "mysupersecretkey" //as allways
grails.plugin.security.stateless.springsecurity.integration = true
grails.plugin.springsecurity.filterChain.chainMap = [
    '/stateless/login': 'statelessLoginFilter'
]
grails.plugin.security.stateless.springsecurity.login.active = true
grails.plugin.security.stateless.springsecurity.login.endpointUrl = "/stateless/login"
grails.plugin.security.stateless.springsecurity.login.usernameField = "user"
grails.plugin.security.stateless.springsecurity.login.passwordField = "password"

```


The login will return 400 (BAD_REQUEST) if there isn't username or password, 401 (UNAUTHORIZED) for wrong username/password, or 200 (OK) for valid username/password. On 200, also return a JSON body with the token:

```
["token":"Bearer eyJ1c2VybmFtZSI6InBhbGJhIn1fMUkwL3FIblpoQ2JYek5hVVVxSUw4TjAvNmk1Y3Qwb0IvamhQVFdUWGpNTT0="]
```
