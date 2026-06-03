package com.ageulin.oauth2_gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class SendHttpUnauthorizedResponseIfClientAuthenticationIsRequiredFilter extends OncePerRequestFilter {
    private final ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();
    private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;
    private final RequestCache requestCache;

    public SendHttpUnauthorizedResponseIfClientAuthenticationIsRequiredFilter(OAuth2AuthorizationRequestResolver authorizationRequestResolver,
                                                                              AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository,
                                                                              RequestCache requestCache) {
        Assert.notNull(requestCache, "requestCache cannot be null");
        this.requestCache = requestCache;
        Assert.notNull(authorizationRequestResolver, "authorizationRequestResolver cannot be null");
        this.authorizationRequestResolver = authorizationRequestResolver;
        Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            Throwable[] causeChain = this.throwableAnalyzer.determineCauseChain(ex);
            ClientAuthorizationRequiredException authzEx = (ClientAuthorizationRequiredException) this.throwableAnalyzer
                    .getFirstThrowableOfType(ClientAuthorizationRequiredException.class, causeChain);
            if (authzEx != null) {
                try {
                    OAuth2AuthorizationRequest authorizationRequest = this.authorizationRequestResolver.resolve(request,
                            authzEx.getClientRegistrationId());
                    if (authorizationRequest == null) {
                        throw ex;
                    }
                    this.requestCache.saveRequest(request, response);

                    if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationRequest.getGrantType())) {
                        this.authorizationRequestRepository.saveAuthorizationRequest(authorizationRequest, request, response);
                    }
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                }
                catch (Exception ex2) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }
    }

    private static final class DefaultThrowableAnalyzer extends ThrowableAnalyzer {

        @Override
        protected void initExtractorMap() {
            super.initExtractorMap();
            registerExtractor(ServletException.class, (throwable) -> {
                ThrowableAnalyzer.verifyThrowableHierarchy(throwable, ServletException.class);
                return ((ServletException) throwable).getRootCause();
            });
        }

    }
}
