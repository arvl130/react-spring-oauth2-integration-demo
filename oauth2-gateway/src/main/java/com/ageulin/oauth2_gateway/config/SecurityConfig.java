package com.ageulin.oauth2_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           ClientRegistrationRepository clientRegistrationRepository) {
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
                .csrf(CsrfConfigurer::spa)
                .logout((logout) -> logout
                        .logoutSuccessHandler(this.oidcLogoutSuccessHandler(clientRegistrationRepository))
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

    private LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        var oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        // Sets the location that the End-User's User Agent will be redirected to
        // after the logout has been performed at the Provider
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        
        // Instead of sending HTTP Status 202 Accepted, instead of a redirect.
        // This allows our frontend to read the Location header and perform
        // the redirect themselves.
        var redirectStrategy = new DefaultRedirectStrategy();
        redirectStrategy.setStatusCode(HttpStatus.ACCEPTED);
        oidcLogoutSuccessHandler.setRedirectStrategy(redirectStrategy);

        return oidcLogoutSuccessHandler;
    }
}
