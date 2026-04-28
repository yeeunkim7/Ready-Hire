package com.devinterview.api.auth.repository;

import com.devinterview.api.auth.entity.UserRefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {

    Optional<UserRefreshToken> findByRefreshToken(String refreshToken);

    Optional<UserRefreshToken> findByUserId(Long userId);
}
