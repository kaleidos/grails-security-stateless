package net.kaleidos.grails.plugin.security.stateless.token

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class StatelessAuthenticationToken extends UsernamePasswordAuthenticationToken {
    String tokenValue
    Map securityStatelessMap

    StatelessAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String tokenValue) {
        super(principal, credentials, authorities)
        this.tokenValue = tokenValue
    }

    StatelessAuthenticationToken(String tokenValue) {
        super("N/A", "N/A")
        this.tokenValue = tokenValue
    }

    StatelessAuthenticationToken(String tokenValue, Collection<? extends GrantedAuthority> authorities) {
        super("N/A", "N/A")
        this.tokenValue = tokenValue
    }

}
