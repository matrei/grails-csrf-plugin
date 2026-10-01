# 🧩 Grails CSRF Protection Plugin

[![Maven Central](https://img.shields.io/maven-central/v/io.github.matrei/grails-csrf.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.matrei/grails-csrf-plugin) [![Java CI](https://github.com/matrei/grails-csrf-plugin/actions/workflows/gradle-check.yml/badge.svg?event=push)](https://github.com/matrei/grails-inertia-plugin/actions/workflows/gradle-check.yml)

Add [CSRF](https://owasp.org/www-community/attacks/csrf) protection to your [Grails](https://grails.apache.org) application.

This plugin will validate that all HTTP requests that changes state (POST, PUT, PATCH and DELETE), includes a valid CSRF token.

By default, any such request that does not include a valid token will be rejected with a `403 Forbidden` status code.

## 📦 Plugin Installation

Add the plugin dependency to the project:

```groovy
dependencies {
    //...
    // Replace $csrfPluginVersion with a suitable release version for your project,
    // or define it in your gradle.properties file
    runtimeOnly "io.github.matrei:grails-csrf:$csrfPluginVersion"
    //...
}
``` 

## 📖 Usage

Using [GSP](https://grails.apache.org/docs/latest/guide/theWebLayer.html#gsp), you can add the CSRF token to the page head, and to forms, using [GSP Tags](https://grails.apache.org/docs/latest/guide/theWebLayer.html#tags).
```html
<html lang="en">
    <head>
        <csrf:headToken/>
    </head>
    <body>
        <form method="POST" action="/books">
            <csrf:formToken/>
            <input type="text" name="title">
            <input type="submit" value="Save"/>
        </form>
    </body>
</html>
```
The token is stored in the HTTP session, and is created the first time one of the tags is rendered.
Requests that do not render a tag do not create a session or a token.

The head `token` can be used by `JavaScript` libraries (like `jQuery`) to automatically make `CSRF`-compatible `Ajax` requests.
```javascript
// jQuery example
$.ajaxSetup({
    headers: {
        'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
    }
});
```
For `SPA`-type applications, the head is typically not refreshed, so the `token` can get stale.

In this case, a `XSRF-TOKEN` cookie can be utilized, by reading it and setting an `X-XSRF-TOKEN` header on the requests (done automatically by [Axios](https://axios-http.com/docs/req_config)).

The plugin can set this cookie, but it is disabled by default.
```yaml
csrf:
  cookie:
    enabled: true # default is false
```
With the cookie enabled, the token (and the session) is created on the first request,
so that it can be sent in the cookie.
The cookie is only sent when the browser does not already have the current token.

### Login and Logout
A token is bound to the user it was issued to, as reported by `request.getUserPrincipal()`
(set by, for example, Spring Security or container authentication).
When the user logs in, logs out or changes, the old token is no longer accepted,
and a new token is created the next time one is needed.
Pages rendered before the change must be reloaded to get the new token.

If your application handles login and logout itself, without setting the user principal,
clear the token when the user changes:
```groovy
class LoginController {

    CsrfSessionHandler csrfSessionHandler

    def login() {
        // ...authenticate the user...
        csrfSessionHandler.clearToken(request)
    }
}
```

### Handling Failures
By default, a request that fails CSRF protection is rejected with `403 Forbidden`,
which you can handle with a `"403"` URL mapping, like any other error status.

To respond differently, register a `CsrfFailureHandler` bean.
It is told why the request was rejected:

| `CsrfFailureReason`     | Meaning                                                                      |
|-------------------------|------------------------------------------------------------------------------|
| `MISSING_STORED_TOKEN`  | No token is stored, typically because the session expired or the user changed |
| `MISSING_REQUEST_TOKEN` | The request does not include a token                                         |
| `INVALID_TOKEN`         | The token in the request does not match the stored token                      |

```groovy
class SessionExpiredCsrfFailureHandler implements CsrfFailureHandler {

    @Override
    void handle(HttpServletRequest request, HttpServletResponse response, CsrfFailureReason reason) {
        if (reason == CsrfFailureReason.MISSING_STORED_TOKEN) {
            response.sendRedirect('/session-expired')
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, reason.message)
        }
    }
}
```
```groovy
class Application extends GrailsAutoConfiguration {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    def beans = {
        bean(CsrfFailureHandler, SessionExpiredCsrfFailureHandler)
    }
}
```
Your code then needs the plugin at compile time, so declare it with `implementation` instead of `runtimeOnly`.

### Excluding URIs from CSRF Protection
Sometimes you may want to exclude certain URIs from CSRF protection.
For example, you may want to exclude a webhook URI that is called by a third-party service.

In that case you can exclude the URI by adding it to the `excluded` list in the configuration.
The excluded URIs are regex matched against the request URI.
```yaml
csrf:
  excluded:
    - '^/webhooks/.*'
```
`/error` is always excluded from CSRF protection.

### Configuration
The following are available configuration options for the plugin (this is the default configuration):
```yaml
csrf:
  fieldName: '_token' # token form field name
  attributeName: 'io.github.matrei.grailsplugin.csrf.token' # session attribute name for token storage
  excluded: [] # paths to exclude from CSRF protection
  cookie:
    enabled: false # set XSRF-TOKEN cookie
    path: '/'
    domain: null # null means the cookie is only sent to the current host, not its subdomains
    secure: true
    sameSite: 'Lax'
```
