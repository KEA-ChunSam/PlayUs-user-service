package com.playus.userservice.domain.notification.controller;

import com.playus.userservice.domain.notification.dto.response.NotificationResponse;
import com.playus.userservice.domain.notification.service.NotificationService;
import com.playus.userservice.domain.notification.specification.NotificationApiControllerSpecification;
import com.playus.userservice.domain.notification.specification.NotificationControllerSpecification;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/user/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationControllerSpecification {

	private final NotificationService notificationService;

	@GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> subscribe(
			@AuthenticationPrincipal CustomOAuth2User principal,
			@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

		Long userId = Long.parseLong(principal.getName());
		SseEmitter emitter = notificationService.subscribe(userId, lastEventId);
		return ResponseEntity.ok(emitter);
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

}
