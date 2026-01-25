package com.ConselhoDaComunidade.JudicialControl.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class TwoFactorAuthenticationToken extends AbstractAuthenticationToken {

    private final String cpf;
    private final String otp;

    // Token não autenticado
    public TwoFactorAuthenticationToken(String cpf, String otp) {
        super(null);
        this.cpf = cpf;
        this.otp = otp;
        setAuthenticated(false);
    }

    // Token autenticado
    public TwoFactorAuthenticationToken(String cpf, String otp,
                                        Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.cpf = cpf;
        this.otp = otp;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return cpf;
    }

    @Override
    public Object getCredentials() {
        return otp;
    }

    public String getOtp() {
        return otp;
    }
}
