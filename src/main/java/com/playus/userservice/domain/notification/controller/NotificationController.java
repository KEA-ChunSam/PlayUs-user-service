package com.playus.userservice.domain.notification.controller;

import com.playus.userservice.domain.notification.dto.response.NotificationResponse;
import com.playus.userservice.domain.notification.service.NotificationService;
import com.playus.userservice.domain.notification.specification.NotificationControllerSpecification;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.feign.response.CommentNotificationEvent;
import com.playus.userservice.domain.user.feign.response.PartyNotificationEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(
			@AuthenticationPrincipal CustomOAuth2User principal,
			@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

		Long userId = Long.parseLong(principal.getName());
		return notificationService.subscribe(userId, lastEventId);
	}

	@PatchMapping("/read/{notification-id}")
	public ResponseEntity<Void> readNotification(
			@AuthenticationPrincipal CustomOAuth2User principal,
			@PathVariable("notification-id") Long notificationId) {

		Long userId = Long.parseLong(principal.getName());
		notificationService.readNotification(userId, notificationId);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<List<NotificationResponse>> getNotifications(
			@AuthenticationPrincipal CustomOAuth2User principal) {

		Long userId = Long.parseLong(principal.getName());
		return ResponseEntity.ok(notificationService.getNotifications(userId));
	}

	@GetMapping("/recent")
	public ResponseEntity<List<NotificationResponse>> getRecentNotifications(
			@AuthenticationPrincipal CustomOAuth2User principal) {

		Long userId = Long.parseLong(principal.getName());
		return ResponseEntity.ok(notificationService.getRecentNotifications(userId));
	}

	@DeleteMapping("/comment/{comment-id}")
	public ResponseEntity<Void> deleteByCommentId(
			@PathVariable("comment-id") Long commentId) {

		notificationService.deleteByCommentId(commentId);
		return ResponseEntity.noContent().build();  // 204 No Content
	}

	// community
	@PostMapping("/comment")
	public ResponseEntity<Void> createCommentNotification(
			@RequestBody CommentNotificationEvent event) {

		notificationService.sendCommentNotification(event);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	// twp
	@PostMapping("/party")
	public ResponseEntity<Void> createPartyNotification(
			@RequestBody PartyNotificationEvent event) {

		notificationService.createPartyNotification(event);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

}
