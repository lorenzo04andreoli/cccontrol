package com.ConselhoDaComunidade.JudicialControl.security;

import com.ConselhoDaComunidade.JudicialControl.entity.User;
import com.ConselhoDaComunidade.JudicialControl.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
                                        Authentication authentication) throws IOException, ServletException {

        String cpf = authentication.getName();
        User user = userRepository.findByCpf(cpf).orElse(null);

        System.out.println("Entrou no CustomAuthenticationSuccessHandler");

        if (user != null && user.isTwoFactorEnabled()) {
            // 2FA já ativo → exigir código
            System.out.println("Usuário com 2FA ativo, redirecionando para /two-factor");

            HttpSession session = request.getSession();
            session.setAttribute("2fa_required", true);
            session.setAttribute("2fa_user", cpf);

            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            response.sendRedirect(request.getContextPath() + "/two-factor");
            return;
        }

        // Usuário sem 2FA → vai direto para o dashboard
        System.out.println("Usuário sem 2FA, indo para /");
        response.sendRedirect(request.getContextPath() + "/");
    }

}
