package com.ConselhoDaComunidade.JudicialControl.security;


import com.ConselhoDaComunidade.JudicialControl.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener {
    private final UserRepository userRepository;

    // Limite de tentativas e tempo de bloqueio
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    @Autowired
    public AuthenticationFailureListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String cpf = String.valueOf(event.getAuthentication().getPrincipal()).replaceAll("\\D", "");

        // Não revelar se o usuário existe
        userRepository.findByCpf(cpf).ifPresent(user -> {
            user.incrementFailedAttempts();

            // Se atingiu o limite, bloqueia
            if (user.getFailedAttempts() >= MAX_ATTEMPTS) {
                user.lockAccount(LOCK_MINUTES);
            }

            userRepository.save(user);
        });
    }
}
