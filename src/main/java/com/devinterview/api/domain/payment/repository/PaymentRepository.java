package com.devinterview.api.domain.payment.repository;

import com.devinterview.api.domain.payment.entity.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 결제 내역 저장소.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPortonePaymentId(String portonePaymentId);

    List<Payment> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
