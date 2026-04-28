package com.devinterview.api.domain.entity;

import com.devinterview.api.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "user_profiles",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_profiles_user_id", columnNames = {"user_id"})
    }
)
public class UserProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "target_company", length = 100)
    private String targetCompany;

    @Column(name = "introduction", columnDefinition = "text")
    private String introduction;
}
