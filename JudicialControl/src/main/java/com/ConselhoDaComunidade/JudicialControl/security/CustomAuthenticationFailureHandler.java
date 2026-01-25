package com.ConselhoDaComunidade.JudicialControl.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Garante mensagens genéricas e tempo uniforme em falhas de login.
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException {

        // Espera fixa para evitar ataques de timing
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Redireciona sempre com a mesma flag genérica
        response.sendRedirect("/login?error=true");
    }
}



