# Grails CSRF Protection Plugin

[![Maven Central](https://img.shields.io/maven-central/v/io.github.matrei/grails-csrf.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.matrei/grails-csrf-plugin) [![Java CI](https://github.com/matrei/grails-csrf-plugin/actions/workflows/gradle-check.yml/badge.svg?event=push)](https://github.com/matrei/grails-inertia-plugin/actions/workflows/gradle-check.yml)

Add [CSRF](https://owasp.org/www-community/attacks/csrf) protection to your [Grails](https://grails.org) application.

This plugin will validate that all HTTP requests that changes state (POST, PUT, PATCH and DELETE), includes a valid CSRF token.

Any such request that does not include a valid token will be rejected with a `419 Forbidden` status code.

## Installation

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

## Usage

Using [GSP](https://gsp.grails.org/latest/guide/index.html), you can add the CSRF token to the page head, and to forms, using custom tags.
```html
<html>
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

This cookie is optionally set by the plugin.
```yaml
csrf:
    cookie:
        enabled: false # default is true
```

## Excluding URIs from CSRF Protection
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

## Configuration
The following are available configuration options for the plugin (this is the default configuration):
```yaml
csrf:
  fieldName: '_token' # token form field name
  attributeName: 'io.github.matrei.grailsplugin.csrf.token' # session attribute name for token storage
  excluded: [] # paths to exclude from CSRF protection
  cookie:
    enabled: true # set XSRF-TOKEN cookie
    path: '/'
    domain: null
    secure: true
    sameSite: 'Lax'
```
