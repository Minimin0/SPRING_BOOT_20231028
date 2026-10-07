package com.example.demo.config;

import com.example.demo.model.domain.Member;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${app.security.remember-me-key}") String rememberMeKey,
            @Value("${app.security.remember-me-duration}") Duration rememberMeDuration) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/hello", "/login", "/signup", "/error",
                                "/detailed_web.html", "/detailed_ai.html",
                                "/detailed_security.html", "/detailed_app.html")
                        .permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole(Member.ROLE_ADMIN)
                        .requestMatchers("/testdb").hasAnyRole(Member.ROLE_ADMIN, Member.ROLE_MANAGER)
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
                        .tokenValiditySeconds(Math.toIntExact(rememberMeDuration.toSeconds())));
        return http.build();
    }
}
