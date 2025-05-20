package com.playus.userservice.domain.user.entity;

import com.playus.userservice.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE favorite_team SET activated = false WHERE id = ?")
@Where(clause = "activated = true")
@Table(name = "favorite_team",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_display_order", columnNames = {"user_id", "display_order"}))
public class FavoriteTeam extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    private FavoriteTeam(User user, Long teamId, Integer displayOrder) {
        this.user = user;
        this.teamId = teamId;
        this.displayOrder = displayOrder;
    }

    public static FavoriteTeam create(User user, Long teamId, Integer order) {
        validateOrder(order);
        return FavoriteTeam.builder()
                .user(user)
                .teamId(teamId)
                .displayOrder(order)
                .build();
    }

    public void update(Long teamId, Integer order) {
        validateOrder(order);
        this.teamId = teamId;
        this.displayOrder = order;
    }

    private static void validateOrder(Integer order) {
        if (order == null || order < 1 || order > 10)
            throw new IllegalArgumentException("선호팀 우선순위는 1‑10 사이여야 합니다.");
    }

}
