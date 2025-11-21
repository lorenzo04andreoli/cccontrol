package com.ConselhoDaComunidade.JudicialControl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String nome;

    @Column(name = "cpf", unique = true, nullable = false)
    @NotEmpty
    private String cpf;

    @NotEmpty
    private String senha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    // Controle de tentativas e bloqueio
    private int failedAttempts = 0;

    private LocalDateTime lockedUntil;

    @Column(name = "totp_secret", length = 128)
    private String totpSecret;

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled = false;


    // ========= MÉTODOS PADRÃO =========
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }

    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getSenha() { return senha; }

    public void setSenha(String senha) { this.senha = senha; }

    public Set<String> getRoles() { return roles; }

    public void setRoles(Set<String> roles) { this.roles = roles; }

    public int getFailedAttempts() { return failedAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }

    // getters / setters
    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    // ========= MÉTODOS DE BLOQUEIO =========

    /** Retorna se a conta está desbloqueada */
    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
    }

    /** Incrementa tentativas de login */
    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }

    /** Reseta tentativas após login bem-sucedido */
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lockedUntil = null;
    }

    /** Bloqueia o usuário por X minutos */
    public void lockAccount(int minutes) {
        this.lockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }

    // ========= SPRING SECURITY =========
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() { return senha; }

    @Override
    public String getUsername() { return cpf; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
