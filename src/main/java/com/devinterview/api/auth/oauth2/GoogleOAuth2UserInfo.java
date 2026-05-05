package com.devinterview.api.auth.oauth2;

import java.util.Map;

/**
 * Google OAuth2 사용자 정보 매핑 클래스.
 */
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "GOOGLE";
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("sub"));
    }

    @Override
    public String getEmail() {
        Object email = attributes.get("email");
        return email == null ? null : String.valueOf(email);
    }

    @Override
    public String getName() {
        Object name = attributes.get("name");
        return name == null ? null : String.valueOf(name);
    }
}
