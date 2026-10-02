package spring.security.app

class UrlMappings {

    static mappings = {
        '/'(view: '/index')
        '/secure'(view: '/secure')
    }
}
