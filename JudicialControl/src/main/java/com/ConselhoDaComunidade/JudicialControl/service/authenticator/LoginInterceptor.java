package com.ConselhoDaComunidade.JudicialControl.service.authenticator;

import com.ConselhoDaComunidade.JudicialControl.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException{
        System.out.println("Interceptando: " + request.getRequestURI());
        if (CookieService.getCookie(request, "userId") != null){
            return true;
        }

        response.sendRedirect("/login");
        return false;
    }
}
