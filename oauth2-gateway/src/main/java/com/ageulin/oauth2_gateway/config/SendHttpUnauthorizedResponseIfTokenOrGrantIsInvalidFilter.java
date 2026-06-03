package com.ageulin.oauth2_gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class SendHttpUnauthorizedResponseIfTokenOrGrantIsInvalidFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ServletException ex) {
            var cause = ex.getCause();
            if (cause instanceof ClientAuthorizationException clientAuthorizationException) {
                var error = clientAuthorizationException.getError();
                var errorCode = error.getErrorCode();

                if (errorCode.equals("invalid_token") || errorCode.equals("invalid_grant")) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                } else {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }
    }
}
