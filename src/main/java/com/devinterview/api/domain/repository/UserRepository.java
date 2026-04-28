package com.devinterview.api.domain.repository;

import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.enums.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndProvider(String email, Provider provider);
}
