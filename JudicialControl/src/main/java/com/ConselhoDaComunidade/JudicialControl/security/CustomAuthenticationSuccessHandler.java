package com.ConselhoDaComunidade.JudicialControl.security;

import com.ConselhoDaComunidade.JudicialControl.entity.User;
import com.ConselhoDaComunidade.JudicialControl.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String cpf = authentication.getName();
        User user = userRepository.findByCpf(cpf).orElse(null);

        if (user != null && user.isTwoFactorEnabled()) {

            // 1) Invalida sessão atual (anti session fixation)
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // 2) Cria nova sessão limpa e guarda só o necessário
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("2fa_required", true);
            newSession.setAttribute("2fa_user", cpf);
            newSession.setAttribute("2fa_started_at", System.currentTimeMillis());

            // 3) Remove qualquer auth residual
            SecurityContextHolder.clearContext();

            response.sendRedirect(request.getContextPath() + "/two-factor");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/");
    }
}
