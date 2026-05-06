package com.devinterview.api.auth.oauth2;

import com.devinterview.api.domain.entity.User;
import com.devinterview.api.security.user.CustomUserDetails;
import java.util.Map;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * OAuth2 인증 principal과 기존 CustomUserDetails를 통합한 구현체.
 */
public class OAuth2UserPrincipal extends CustomUserDetails implements OAuth2User {

    private final Map<String, Object> attributes;

    public OAuth2UserPrincipal(User user, Map<String, Object> attributes) {
        super(user);
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return getUsername();
    }
}
