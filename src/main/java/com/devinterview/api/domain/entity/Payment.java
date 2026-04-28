package com.devinterview.api.domain.entity;

import com.devinterview.api.domain.common.BaseTimeEntity;
import com.devinterview.api.domain.converter.PaymentStatusConverter;
import com.devinterview.api.domain.converter.PaymentTypeConverter;
import com.devinterview.api.domain.enums.PaymentStatus;
import com.devinterview.api.domain.enums.PaymentType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "payments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payments_provider_payment_id", columnNames = {"provider_payment_id"})
    }
)
@Check(constraints = "payment_type in ('SUBSCRIPTION','REFUND')")
@Check(constraints = "status in ('PENDING','SUCCESS','FAILED','CANCELED')")
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Convert(converter = PaymentTypeConverter.class)
    @Column(name = "payment_type", nullable = false, length = 20)
    private PaymentType paymentType;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Convert(converter = PaymentStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "provider_payment_id", length = 100)
    private String providerPaymentId;

    @Column(name = "paid_at", columnDefinition = "timestamptz")
    private OffsetDateTime paidAt;
}
