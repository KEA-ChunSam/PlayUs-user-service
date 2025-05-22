package com.playus.userservice.domain.oauth.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend.fail-redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.warn("OAuth2 로그인 실패: {}", exception.getMessage());

        String redirectUrl = redirectUri;

        if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauthEx) {
            String errorCode = oauthEx.getError().getErrorCode();
            String description = oauthEx.getError().getDescription();

            switch (errorCode) {
                case "user_deactivated" -> {
                    String encodedNickname = java.net.URLEncoder.encode(description, java.nio.charset.StandardCharsets.UTF_8);
                    redirectUrl = redirectUri + "?error=withdrawn&nickname=" + encodedNickname;
                }
                case "provider_mismatch" -> {
                    redirectUrl = redirectUri + "?error=provider_mismatch";
                }
                case "missing_phone" -> {
                    redirectUrl = redirectUri + "?error=missing_phone";
                }
                default -> {
                    redirectUrl = redirectUri + "?error=unknown";
                }
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
