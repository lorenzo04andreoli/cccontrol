package com.ConselhoDaComunidade.JudicialControl.security;

import com.ConselhoDaComunidade.JudicialControl.entity.User;
import com.ConselhoDaComunidade.JudicialControl.service.DatabaseUserDetailsService;
import com.ConselhoDaComunidade.JudicialControl.util.TOTPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class TwoFactorAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private DatabaseUserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (!(authentication instanceof TwoFactorAuthenticationToken token)) return null;

        String cpf = String.valueOf(token.getPrincipal()).replaceAll("\\D", "");
        String otp = String.valueOf(token.getCredentials());

        User user = (User) userDetailsService.loadUserByUsername(cpf);

        if (!user.isTwoFactorEnabled() || user.getTotpSecret() == null) {
            throw new org.springframework.security.authentication.BadCredentialsException("2FA não habilitado");
        }

        String code = (otp == null) ? "" : otp.replaceAll("\\D", "");
        if (code.length() != 6) {
            throw new org.springframework.security.authentication.BadCredentialsException("OTP inválido");
        }

        boolean ok = TOTPUtil.verifyCode(user.getTotpSecret(), Integer.parseInt(code), 2);
        if (!ok) {
            throw new org.springframework.security.authentication.BadCredentialsException("OTP inválido");
        }

        // autenticação final
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return TwoFactorAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
