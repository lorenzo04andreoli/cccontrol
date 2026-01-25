package com.ConselhoDaComunidade.JudicialControl.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class TwoFactorGateFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_PREFIXES = Set.of(
            "/two-factor",
            "/logout",
            "/login",
            "/logar",
            "/css/",
            "/js/",
            "/images/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String ctx = request.getContextPath();


        if (ctx != null && !ctx.isBlank() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }

        HttpSession session = request.getSession(false);

        boolean twoFactorRequired = session != null && Boolean.TRUE.equals(session.getAttribute("2fa_required"));

        if (twoFactorRequired && !isAllowed(path)) {
            response.sendRedirect((ctx == null ? "" : ctx) + "/two-factor");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String path) {
        for (String p : ALLOWED_PREFIXES) {
            if (path.equals(p) || path.startsWith(p)) return true;
        }
        return false;
    }
}


