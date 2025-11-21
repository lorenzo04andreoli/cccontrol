package com.ConselhoDaComunidade.JudicialControl.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventListener.class);

    private String mascararCpf(String cpf) {
        return cpf.length() == 11 ? "***" + cpf.substring(3, 9) + "**" : "usuário";
    }

    @EventListener
    public void handleAuthSuccess(AuthenticationSuccessEvent event) {
        log.info("LOGIN BEM SUCEDIDO: usuário [{}]", mascararCpf( event.getAuthentication().getName()));
    }

    @EventListener
    public void handleAuthFailure(AuthenticationFailureBadCredentialsEvent event) {
        log.warn("FALHA DE LOGIN: usuário [{}]", event.getAuthentication().getName());
    }

    @EventListener
    public void handleSessionCreated(HttpSessionCreatedEvent event) {
        log.info("NOVA SESSÃO CRIADA: ID={}", event.getSession().getId());
    }

    @EventListener
    public void handleSessionDestroyed(HttpSessionDestroyedEvent event) {
        log.info("SESSÃO ENCERRADA: ID={}", event.getSession().getId());
    }
}