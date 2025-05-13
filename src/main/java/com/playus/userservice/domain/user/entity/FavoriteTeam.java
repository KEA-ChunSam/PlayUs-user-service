package com.playus.userservice.domain.user.entity;

import com.playus.userservice.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "favorite_team",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_display_order", columnNames = {"user_id", "display_order"}))
public class FavoriteTeam extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;


    @Builder
    private FavoriteTeam(Long userId, Long teamId, Integer displayOrder) {
        this.userId = userId;
        this.teamId = teamId;
        this.displayOrder = displayOrder;
    }

    public static FavoriteTeam create(Long userId, Long teamId, Integer order) {
        validateOrder(order);
        return FavoriteTeam.builder()
                .userId(userId)
                .teamId(teamId)
                .displayOrder(order)
                .build();
    }

    public void update(Long teamId, Integer order) {
        validateOrder(order);
        this.teamId       = teamId;
        this.displayOrder = order;
    }

    private static void validateOrder(Integer order) {
        if (order == null || order < 1 || order > 10)
            throw new IllegalArgumentException("선호팀 우선순위는 1‑10 사이여야 합니다.");
    }
}
