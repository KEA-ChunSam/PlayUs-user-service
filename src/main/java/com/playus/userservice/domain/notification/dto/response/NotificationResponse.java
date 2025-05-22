package com.playus.userservice.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.playus.userservice.domain.user.entity.Notification;
import com.playus.userservice.domain.user.enums.NotificationType;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public record NotificationResponse(
	Long id,
	String title,
	String content,
	Long commentId,
	Long partyId,
	NotificationType type,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	LocalDateTime createdAt,
	boolean isRead
) {
	public static NotificationResponse from(Notification entity) {
		return new NotificationResponse(
				entity.getId(),
				entity.getTitle(),
				entity.getContent(),
				entity.getCommentId(),
				entity.getPartyId(),
				entity.getType(),
				entity.getCreatedAt(),
				entity.isRead()
		);
	}
}
