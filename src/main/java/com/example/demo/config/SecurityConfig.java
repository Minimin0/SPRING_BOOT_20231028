package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${app.security.remember-me-key}") String rememberMeKey) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/hello", "/login", "/signup", "/error",
                                "/detailed_web.html", "/detailed_ai.html",
                                "/detailed_security.html", "/detailed_app.html")
                        .permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me"))
                .rememberMe(remember -> remember
                        .key(rememberMeKey)
                        .tokenValiditySeconds(7 * 24 * 60 * 60));
        return http.build();
    }
}
