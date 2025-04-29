package com.playus.userservice.domain.report.entity;

import com.playus.userservice.domain.common.BaseTimeEntity;
import com.playus.userservice.domain.report.enums.TargetType;
import com.playus.userservice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reports")
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false, name = "target_id")
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "target_type")
    private TargetType targetType;

    @Builder
    private Report(User user, String text, Long targetId, TargetType targetType) {
        this.user = user;
        this.text = text;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    public static Report create(User user, String text, Long targetId, TargetType targetType) {
        return Report.builder()
                .user(user)
                .text(text)
                .targetId(targetId)
                .targetType(targetType)
                .build();
    }

    public void updateAll(Report report) {
        this.text = report.getText();
        this.targetId = report.getTargetId();
        this.targetType = report.getTargetType();
    }
}
