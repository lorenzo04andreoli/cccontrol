package com.ConselhoDaComunidade.JudicialControl.security;

import com.ConselhoDaComunidade.JudicialControl.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessListener {
    private final UserRepository userRepository;

    @Autowired
    public LoginSuccessListener(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event){
        String cpf = event.getAuthentication().getName();

        userRepository.findByCpf(cpf).ifPresent(user -> {
            if (user.getFailedAttempts()>0 || user.getLockedUntil() != null){
                user.resetFailedAttempts();
                userRepository.save(user);
            }
        });
    }
}
