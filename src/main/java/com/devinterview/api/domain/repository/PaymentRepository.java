package com.devinterview.api.domain.repository;

import com.devinterview.api.domain.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPgPaymentKey(String pgPaymentKey);
}
