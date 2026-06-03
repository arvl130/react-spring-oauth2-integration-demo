package com.ageulin.oauth2_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;

@Configuration
public class SecurityConfig {
    @Bean
    OAuth2AuthorizationRequestResolver authorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        return new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }

    @Bean
    AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

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
                // Enable OIDC Logout via Back-channel Endpoint.
                .oidcLogout((logout) -> logout
                        .backChannel(Customizer.withDefaults())
                )
                // Enable CSRF settings required for single page applications.
                .csrf(CsrfConfigurer::spa)
                // Perform OIDC RP-initiated Logout after Local Logout succeeds.
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
                )
                // Spring Security throws a ClientAuthorizationException if the session is still valid,
                // but our OAuth2 Client fails to authorize with the error code invalid_token or invalid_grant.
                // The default behavior is to respond with an HTTP status Internal Server Error response when
                // this exception is thrown.
                //
                // Instead, we will respond with an HTTP status Unauthorized response, because our frontend
                // expects this response if our token has become invalid.
                .addFilterBefore(
                        new SendHttpUnauthorizedResponseIfTokenOrGrantIsInvalidFilter(),
                        OAuth2AuthorizationRequestRedirectFilter.class
                );

        // Initialize the default request cache and make it available.
        http.requestCache(configurer -> {
            configurer.init(http);
        });

        RequestCache defaultRequestCache = http.getSharedObject(RequestCache.class);

        // Spring Security throws a ClientAuthorizationRequiredException if the session is still valid,
        // but there is no authorized OAuth2 Client available. The default behavior is to respond with
        // an HTTP redirect response when this exception is thrown.
        //
        // Instead, we will respond with an HTTP status Unauthorized response, because our frontend
        // expects this response if our token has been removed from the session.
        var oauth2RequiredFilter = new SendHttpUnauthorizedResponseIfClientAuthenticationIsRequiredFilter(
                this.authorizationRequestResolver(clientRegistrationRepository),
                this.authorizationRequestRepository(),
                defaultRequestCache
        );

        http.addFilterAfter(oauth2RequiredFilter, OAuth2AuthorizationRequestRedirectFilter.class);
        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        var oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        // Sets the location that the End-User's User Agent will be redirected to
        // after the logout has been performed at the Provider
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        
        // Send HTTP Status 202 Accepted instead of a redirect.
        //
        // This allows our frontend to read the Location header and perform
        // the redirect itself.
        var redirectStrategy = new DefaultRedirectStrategy();
        redirectStrategy.setStatusCode(HttpStatus.ACCEPTED);
        oidcLogoutSuccessHandler.setRedirectStrategy(redirectStrategy);

        return oidcLogoutSuccessHandler;
    }
}
