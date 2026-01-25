package com.ConselhoDaComunidade.JudicialControl.config;

import com.ConselhoDaComunidade.JudicialControl.security.CustomAuthenticationFailureHandler;
import com.ConselhoDaComunidade.JudicialControl.security.CustomAuthenticationSuccessHandler;
import com.ConselhoDaComunidade.JudicialControl.security.TwoFactorAuthenticationProvider;
import com.ConselhoDaComunidade.JudicialControl.security.TwoFactorGateFilter;
import com.ConselhoDaComunidade.JudicialControl.service.DatabaseUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.session.HttpSessionEventPublisher;


@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            DatabaseUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }


    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider daoAuthProvider,
            TwoFactorAuthenticationProvider twoFactorAuthenticationProvider
    ) {

        return new ProviderManager(twoFactorAuthenticationProvider, daoAuthProvider);
    }

    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/logar", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/two-factor", "/two-factor/verify").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reeducandos/**").hasAnyRole("ADMIN", "USER", "VIEWER")
                        .requestMatchers(HttpMethod.POST, "/reeducandos/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.PUT, "/reeducandos/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.DELETE, "/reeducandos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/relatorios/**").hasAnyRole("ADMIN", "USER", "VIEWER", "AUDIENCE")
                        .requestMatchers(HttpMethod.GET, "/usuario").hasAnyRole("ADMIN","USER","VIEWER", "AUDIENCE")
                        .requestMatchers(HttpMethod.POST, "/usuario/foto").hasAnyRole("ADMIN","USER","VIEWER", "AUDIENCE")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/logar")
                        .usernameParameter("cpf")
                        .passwordParameter("senha")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(failureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?invalid")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired")
                        .sessionRegistry(sessionRegistry())
                )
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self' https://cdnjs.cloudflare.com https://fonts.googleapis.com; " +
                                        "font-src 'self' https://cdnjs.cloudflare.com https://fonts.gstatic.com; " +
                                        "img-src 'self' data: https:; " +
                                        "object-src 'none'; " +
                                        "base-uri 'self'; " +
                                        "frame-ancestors 'none'; " +
                                        "form-action 'self'; " +
                                        "upgrade-insecure-requests; " +
                                        "block-all-mixed-content"
                        ))
                        .permissionsPolicyHeader(pp -> pp.policy(
                                "geolocation=(), microphone=(), camera=(), payment=(), usb=()"
                        ))
                        .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .frameOptions(frame -> frame.deny())
                        .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"))
                )
                .addFilterBefore(new TwoFactorGateFilter(), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}