package com.devinterview.api.auth.oauth2;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.enums.Provider;
import com.devinterview.api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * OAuth2 사용자 정보를 로컬 User 엔티티와 동기화하는 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = resolveOAuth2UserInfo(registrationId, oauth2User);
        String email = userInfo.getEmail();
        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.OAUTH2_EMAIL_NOT_FOUND);
        }

        String normalizedEmail = email.trim().toLowerCase();
        User user = resolveOAuthUser(normalizedEmail, userInfo);

        log.info("OAuth2 login success: provider={}, email={}, userId={}", userInfo.getProvider(), normalizedEmail, user.getId());
        return new OAuth2UserPrincipal(user, oauth2User.getAttributes());
    }

    private OAuth2UserInfo resolveOAuth2UserInfo(String registrationId, OAuth2User oauth2User) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return new GoogleOAuth2UserInfo(oauth2User.getAttributes());
        }
        throw new CustomException(ErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED);
    }

    private User resolveOAuthUser(String email, OAuth2UserInfo userInfo) {
        return userRepository.findByEmailAndProvider(email, Provider.GOOGLE)
            .map(existing -> updateExistingGoogleUser(existing, userInfo))
            .orElseGet(() -> registerGoogleUser(email, userInfo));
    }

    private User updateExistingGoogleUser(User user, OAuth2UserInfo userInfo) {
        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.ACCOUNT_WITHDRAWN);
        }
        user.setProviderUserId(userInfo.getProviderId());
        return userRepository.save(user);
    }

    private User registerGoogleUser(String email, OAuth2UserInfo userInfo) {
        userRepository.findByEmailAndProvider(email, Provider.LOCAL)
            .filter(user -> !user.isDeleted())
            .ifPresent(user -> {
                throw new CustomException(
                    ErrorCode.EMAIL_ALREADY_EXISTS,
                    "이미 이메일로 가입된 계정입니다. 이메일 로그인을 이용해 주세요."
                );
            });

        return createNewSocialUser(email, userInfo);
    }

    private User createNewSocialUser(String email, OAuth2UserInfo userInfo) {
        User user = User.builder()
            .email(email)
            .passwordHash(null)
            .provider(Provider.GOOGLE)
            .providerUserId(userInfo.getProviderId())
            .build();
        return userRepository.save(user);
    }
}
