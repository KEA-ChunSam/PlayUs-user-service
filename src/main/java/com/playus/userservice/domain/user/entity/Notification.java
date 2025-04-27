package com.playus.userservice.domain.user.entity;

import com.playus.userservice.domain.common.BaseTimeEntity;
import com.playus.userservice.domain.user.enums.Type;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(nullable = false, name = "is_read")
    private boolean isRead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Builder
    private Notification(User user, String message, boolean isRead, Type type) {
        this.user = user;
        this.message = message;
        this.isRead = isRead;
        this.type = type;
    }

    public static Notification create(User user, String message, Type type) {
        return Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .type(type)
                .build();
    }

    public void updateAll(Notification notification) {
        this.message = notification.getMessage();
        this.isRead = notification.isRead();
        this.type = notification.getType();
        this.user = notification.getUser();
    }
}
