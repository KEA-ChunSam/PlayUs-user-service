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

    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, name = "is_read")
    private boolean isRead;

    @Builder
    private Notification(User receiver, Long commentId, String content, boolean isRead, NotificationType type) {
        this.receiver = receiver;
        this.commentId = commentId;
        this.content = content;
        this.isRead = isRead;
        this.type = type;
    }

    public static Notification create(User receiver, Long commentId, String content, NotificationType type) {
        return Notification.builder()
                .receiver(receiver)
                .commentId(commentId)
                .content(content)
                .isRead(false)
                .type(type)
                .build();
    }

    public void updateContent(String content, boolean isRead, NotificationType type) {
        this.content = content;
        this.isRead = isRead;
        this.type = type;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
