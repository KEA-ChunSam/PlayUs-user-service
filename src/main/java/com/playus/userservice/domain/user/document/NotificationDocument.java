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
    @Field("title")
    @Size(min = 1, max = 255)
    private String title;

    @NotNull
    @Field("content")
    private String content;

    @Field("comment_id")
    private Long commentId;

    @NotNull
    @Field("receiver_id")
    private Long receiverId;

    @Field("is_read")
    private boolean isRead;

    @NotNull
    private NotificationType type;

    @Builder
    private NotificationDocument(Long id, String title, String content, Long commentId, Long receiverId, boolean isRead, NotificationType type) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.commentId = commentId;
        this.receiverId = receiverId;
        this.isRead = isRead;
        this.type = type;
    }

    public static NotificationDocument createNotificationDocument(Long id, String title, String content, Long commentId, Long receiverId, boolean isRead, NotificationType type) {
        return NotificationDocument.builder()
                .id(id)
                .title(title)
                .content(content)
                .commentId(commentId)
                .receiverId(receiverId)
                .isRead(isRead)
                .type(type)
                .build();
    }
}
