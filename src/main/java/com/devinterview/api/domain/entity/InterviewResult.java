package com.devinterview.api.domain.entity;

import com.devinterview.api.domain.common.BaseTimeEntity;
import com.devinterview.api.domain.converter.GradeConverter;
import com.devinterview.api.domain.enums.Grade;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "interview_results",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_interview_results_interview_id", columnNames = {"interview_id"})
    }
)
@Check(constraints = "grade in ('A','B','C','D','F')")
public class InterviewResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Convert(converter = GradeConverter.class)
    @Column(name = "grade", nullable = false, length = 1)
    private Grade grade;

    @Column(name = "overall_feedback", nullable = false, columnDefinition = "text")
    private String overallFeedback;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detailed_feedback", columnDefinition = "jsonb")
    private Map<String, Object> detailedFeedback;

    @Column(name = "evaluated_at", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime evaluatedAt;
}
