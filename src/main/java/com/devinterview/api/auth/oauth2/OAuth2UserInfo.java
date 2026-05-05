package com.devinterview.api.auth.oauth2;

/**
 * OAuth2 제공자별 사용자 정보 추상화 인터페이스.
 */
public interface OAuth2UserInfo {

    String getProvider();

    String getProviderId();

    String getEmail();

    String getName();
}
