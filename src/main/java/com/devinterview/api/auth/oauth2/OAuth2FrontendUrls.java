package com.devinterview.api.auth.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OAuth2FrontendUrls {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    public String callbackUrl() {
        return redirectUri;
    }

    public String loginUrl(String errorCode) {
        String frontendBase = redirectUri.replace("/oauth2/callback", "");
        return frontendBase + "/login?error=" + errorCode;
    }
}
