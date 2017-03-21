grails-security-stateless
=========================
[![Build Status](https://travis-ci.org/kaleidos/grails-security-stateless.svg?branch=master)](https://travis-ci.org/kaleidos/grails-security-stateless)

  Grails plugin to implement *really* stateless authentication. It is based on the procedure described at http://www.kaleidos.net/blog/295/stateless-authentication-with-api-rest/

   Long story short: we do not keep tokens. We generate a self-contained token, sign it, and give it to the client. The client then send this token on every request. As the token is signed, the client can't mess with it. And as the token is self-contained, we don't need to keep it, only verify it.



   In general, when you try to access a protected method, you will get a 401 (UNAUTHORIZED) if there isn't a valid token on the Authorization header.


Standalone
----------

#### Configuration

The plugin can be used on a pure stand alone mode. For this, you only need to set two parameters on Config.groovy:

```groovy
grails.plugin.security.stateless.secretKey = "mysupersecretkey"
grails.plugin.springsecurity.active = false
```


#### Secure controllers and methods

You can mark any controller or method with @SecuredStateless, and the plugin will keep the authentication for you, adding to the request an attribute 'securityStatelessMap' with the user data.

```groovy
import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless

class HelloController {
    @SecuredStateless
    def index() {
        render "hello ${request.securityStatelessMap.username}"
    }
}
```


#### Login

On standalone mode, you have to code your own login controller. This controller should validate the credentials as you need, and return a security-stateless token, using the method statelessTokenProvider.generateToken(String username).

A silly example of login controller could be:

```groovy
import grails.converters.JSON

class LoginController {
    def statelessTokenProvider

    def index() {
        if (params.password == '12345'){
            render (['token': statelessTokenProvider.generateToken(params.user)] as JSON)
        } else {
            render(status: 401, text: '')
        }
    }
}
```

Integrated with springsecurity
------------------------------


#### Configuration

The plugin can be used along with springsecurity. In order to do so, you need to set several parameters on application.yml:

```yml
grails:
  plugin:
    security:
      stateless:
        format: JWT
        secretKey: mysupersecretkey
        springsecurity:
          integration: true
          saltField: tokenSalt
          login:
            active: true
            endpointUrl: /stateless/login
            usernameField: user
            passwordField: password
          invalidateOnLogin: false
          invalidate:
            active: true
            endpointUrl: /auth/invalidate
```

#### Salt
Your User class should have a 'salt' field for the password salt. You can specify the name of this field on:

* saltField: The name of the salt field

#### Secure controllers and methods

Any method controlled by the defined chainMap is secured. And inside it, you can access both springSecurityService.currentUser and request.securityStatelessMap.


```groovy
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

You can code your own login controller. But the plugin offers you a default method for login. In order to use it, you only need to configure several parameters on application.yml:


* active: true for activate login
* endpointUrl: the login url
* usernameField: The name of the parameter for the username. By default it is "user".
* passwordField: The name of the parameter for the username. By default it is "password".


In order to make a login, your client needs to make a POST to the endpointUrl, with the json body:

```JSON
{
  "user": "johndoe",
  "password": "abcde"
}
```

The login will return 400 (BAD_REQUEST) if there isn't username or password, 401 (UNAUTHORIZED) for wrong username/password, or 201 (CREATED) for valid username/password. On 201, also return a JSON body with the token:

```groovy
["token":"eyJ1c2VybmFtZSI6InBhbGJhIn1fMUkwL3FIblpoQ2JYek5hVVVxSUw4TjAvNmk1Y3Qwb0IvamhQVFdUWGpNTT0="]
```

Your client should add this token on the Authorization header in order to make subsequent authenticated calls to the server.

```
Authorization: "Bearer: eyJ1c2VybmFtZSI6InBhbGJhIn1fMUkwL3FIblpoQ2JYek5hVVVxSUw4TjAvNmk1Y3Qwb0IvamhQVFdUWGpNTT0="
```


#### Invalidation

You can code your own invalidate controller. But the plugin offers you a default method for invalidate a token. In order to use it, you only need to configure several parameters on application.yml:


* active: true for activate invalidation
* endpointUrl: the invalidation url


##### Invalidation on log in

In certain security scenarios we want to invalidate the user when another user logs into the platform. Activating
the following flag the salt will be renewed invalidating all of previous tokens when the user logs in.

```yml
grails:
  plugin:
    security:
      stateless:
        springsecurity:
          invalidateOnLogin: true
```

#### Token format
Currently the plugin supports two token formats:

##### Legacy format (default)
Encrypted internal format. This format is recommended when your extraData field could have sensitive data.

Custom format for internal representation of the token, encoded an encrypted using [PBKDF2 (Password-Based Key Derivation Function 2)](http://en.wikipedia.org/wiki/PBKDF2).

This is the default configuration but if you want to explicitely activate it you can set it on the Config.groovy file:

```yml
grails:
  plugin:
    security:
      stateless:
        format: Legacy
```


##### JWT format
Standarized format using the format defined in the [JWT specification](http://jwt.io). Uses the HS256 as the implementations algorithm

You can activate this format on your application.yml file as follows:

```yml
grails:
  plugin:
    security:
      stateless:
        format: JWT
```

#### Additional configuration
##### Expiration time
By default stateless tokens are configured so they don't expire. You can add an expiration time so your
tokens expires after the indicated minutes.

```yml
grails:
  plugin:
    security:
      stateless:
        expirationTime: 1440 // 1 day
```

##### Expiration response code
If you have configured the expiration time you can additionaly configure the response code that will be
retrieved when a token expires.

By default it's a ```401 Unauthorized``` status but you can choose another like 419 or 498

```yml
grails:
  plugin:
    security:
      stateless:
        expiresStatusCode: 401
```
