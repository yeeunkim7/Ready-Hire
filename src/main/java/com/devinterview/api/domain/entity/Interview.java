package com.devinterview.api.domain.entity;

import com.devinterview.api.domain.common.BaseTimeEntity;
import com.devinterview.api.domain.converter.CareerLevelConverter;
import com.devinterview.api.domain.converter.InterviewStatusConverter;
import com.devinterview.api.domain.converter.InterviewTypeConverter;
import com.devinterview.api.domain.enums.CareerLevel;
import com.devinterview.api.domain.enums.InterviewStatus;
import com.devinterview.api.domain.enums.InterviewType;
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
@Table(name = "interviews")
@Check(constraints = "interview_type in ('TECHNICAL','BEHAVIORAL','SYSTEM_DESIGN','MIXED')")
@Check(constraints = "career_level in ('JUNIOR','MID','SENIOR','LEAD')")
@Check(constraints = "status in ('IN_PROGRESS','COMPLETED','FAILED')")
public class Interview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Convert(converter = InterviewTypeConverter.class)
    @Column(name = "interview_type", nullable = false, length = 20)
    private InterviewType interviewType;

    @Convert(converter = CareerLevelConverter.class)
    @Column(name = "career_level", nullable = false, length = 20)
    private CareerLevel careerLevel;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "job_position", nullable = false, length = 100)
    private String jobPosition;

    @Convert(converter = InterviewStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private InterviewStatus status;
}
