package com.playus.userservice.domain.notification.controller;

import com.playus.userservice.domain.notification.service.NotificationService;
import com.playus.userservice.domain.notification.specification.NotificationApiControllerSpecification;
import com.playus.userservice.domain.user.feign.response.CommentNotificationEvent;
import com.playus.userservice.domain.user.feign.response.PartyNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api")
@RequiredArgsConstructor
public class NotificationApiController implements NotificationApiControllerSpecification {

	private final NotificationService notificationService;

	@DeleteMapping("/notifications/comment/{comment-id}")
	public ResponseEntity<Void> deleteByCommentId(
			@PathVariable("comment-id") Long commentId) {

		notificationService.deleteByCommentId(commentId);
		return ResponseEntity.noContent().build();  // 204 No Content
	}

	// community
	@PostMapping("/notifications/comment")
	public ResponseEntity<Void> createCommentNotification(
			@RequestBody CommentNotificationEvent event) {

		notificationService.sendCommentNotification(event);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	// twp
	@PostMapping("/notifications/party")
	public ResponseEntity<Void> createPartyNotification(
			@RequestBody PartyNotificationEvent event) {

		notificationService.createPartyNotification(event);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

}
