package com.playus.userservice.domain.user.document;

import com.playus.userservice.domain.common.BaseTimeEntity;
import com.playus.userservice.domain.user.enums.NotificationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "notifications")
public class NotificationDocument extends BaseTimeEntity {
    @Id
    private Long id;

    @NotNull
    @Field("receiver_id")
    private Long receiverId;

    @NotNull
    @Size(min = 1, max = 255)
    private String message;

    @Field("is_read")
    private boolean isRead;

    @NotNull
    private NotificationType notificationType;

    @Builder
    private NotificationDocument(Long id, Long receiverId, String message, boolean isRead, NotificationType notificationType) {
        this.id = id;
        this.receiverId = receiverId;
        this.message = message;
        this.isRead = isRead;
        this.notificationType = notificationType;
    }

    public static NotificationDocument createNotificationDocument(Long id, Long receiverId, String message, boolean isRead, NotificationType notificationType) {
        return NotificationDocument.builder()
                .id(id)
                .receiverId(receiverId)
                .message(message)
                .isRead(isRead)
                .type(notificationType)
                .build();
    }
}
