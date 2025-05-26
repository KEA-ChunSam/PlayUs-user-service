package com.playus.userservice.domain.user.entity;

import com.playus.userservice.domain.common.BaseTimeEntity;
import com.playus.userservice.domain.user.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    /** 댓글 알림일 때만 사용 */
    @Column(name = "comment_id")
    private Long commentId;

    /** 직관팟 알림일 때만 사용 */
    @Column(name = "party_id")
    private Long partyId;

    /** 알림을 발생시킨 사용자(참가자·작성자 등) */
    @Column(name = "actor_id")
    private Long actorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, name = "is_read")
    private boolean isRead;

    @Builder
    private Notification(User receiver, String title, String content, Long commentId, Long partyId, Long actorId, boolean isRead, NotificationType type) {
        this.receiver = receiver;
        this.title     = title;
        this.content = content;
        this.commentId = commentId;
        this.partyId    = partyId;
        this.actorId    = actorId;
        this.isRead = isRead;
        this.type = type;
    }

    public static Notification create(User receiver, String title, String content, Long commentId, Long partyId, Long actorId, NotificationType type) {
        return Notification.builder()
                .receiver(receiver)
                .title(title)
                .content(content)
                .commentId(commentId)
                .partyId(partyId)
                .actorId(actorId)
                .isRead(false)
                .type(type)
                .build();
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
