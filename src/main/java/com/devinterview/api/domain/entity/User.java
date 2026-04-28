package com.devinterview.api.domain.entity;

import com.devinterview.api.domain.common.BaseTimeEntity;
import com.devinterview.api.domain.converter.AccountStatusConverter;
import com.devinterview.api.domain.converter.PlanTypeConverter;
import com.devinterview.api.domain.converter.ProviderConverter;
import com.devinterview.api.domain.converter.RoleConverter;
import com.devinterview.api.domain.enums.AccountStatus;
import com.devinterview.api.domain.enums.PlanType;
import com.devinterview.api.domain.enums.Provider;
import com.devinterview.api.domain.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email_provider", columnNames = {"email", "provider"})
    }
)
@Check(constraints = "provider in ('LOCAL','GOOGLE','KAKAO','NAVER')")
@Check(constraints = "role in ('USER','ADMIN')")
@Check(constraints = "account_status in ('ACTIVE','INACTIVE','SUSPENDED','WITHDRAWN')")
@Check(constraints = "plan_type in ('FREE','PRO')")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Convert(converter = ProviderConverter.class)
    @Column(name = "provider", nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_user_id", length = 100)
    private String providerUserId;

    @Convert(converter = RoleConverter.class)
    @Builder.Default
    @Column(name = "role", nullable = false, length = 20)
    @ColumnDefault("'USER'")
    private Role role = Role.USER;

    @Convert(converter = AccountStatusConverter.class)
    @Builder.Default
    @Column(name = "account_status", nullable = false, length = 20)
    @ColumnDefault("'ACTIVE'")
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Convert(converter = PlanTypeConverter.class)
    @Builder.Default
    @Column(name = "plan_type", nullable = false, length = 10)
    @ColumnDefault("'FREE'")
    private PlanType planType = PlanType.FREE;
}
