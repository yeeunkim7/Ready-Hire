package com.devinterview.api.domain.entity;

import com.devinterview.api.domain.common.BaseTimeEntity;
import com.devinterview.api.domain.converter.SubscriptionPlanTypeConverter;
import com.devinterview.api.domain.converter.SubscriptionStatusConverter;
import com.devinterview.api.domain.enums.SubscriptionPlanType;
import com.devinterview.api.domain.enums.SubscriptionStatus;
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
@Table(name = "subscriptions")
@Check(constraints = "status in ('ACTIVE','CANCELED','EXPIRED')")
@Check(constraints = "plan_type in ('MONTHLY','YEARLY')")
public class Subscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Convert(converter = SubscriptionPlanTypeConverter.class)
    @Column(name = "plan_type", nullable = false, length = 20)
    private SubscriptionPlanType planType;

    @Convert(converter = SubscriptionStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "started_at", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime startedAt;

    @Column(name = "ended_at", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime endedAt;

    @Column(name = "canceled_at", columnDefinition = "timestamptz")
    private OffsetDateTime canceledAt;
}
