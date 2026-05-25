package com.ageulin.oauth2_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                // Protect all endpoints.
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().authenticated()
                )
                // Configure OAuth2 Login.
                .oauth2Login(Customizer.withDefaults())
                // Configure OIDC Logout via Back-channel Endpoint.
                .oidcLogout((logout) -> logout
                        .backChannel(Customizer.withDefaults())
                )
                // If the incoming request requires authentication, send an HTTP 401 response.
                //
                // This overrides the default behavior of sending a redirect to the Authorization Endpoint.
                // This part is important, because we do not want our React frontend to receive a redirect
                // response if it tries to send an unauthenticated request to our backend.
                .exceptionHandling(configure -> configure
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                        )
                );

        return http.build();
    }
}
