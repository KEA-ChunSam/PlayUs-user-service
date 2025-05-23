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
public class NotificationController implements NotificationControllerSpecification {

	private final NotificationService notificationService;

	@GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@Operation(summary = "SSE 연결", description = "실시간 알림을 위한 SSE(Server-Sent Events)에 연결합니다.")
	public ResponseEntity<SseEmitter> subscribe(
			@AuthenticationPrincipal CustomOAuth2User principal,
			@Parameter(description = "EventStream이 끊어졌을 때, 클라이언트가 보관한 마지막 ID", example = "2_1747732045603")
			@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

		Long userId = Long.parseLong(principal.getName());
		SseEmitter emitter = notificationService.subscribe(userId, lastEventId);
		return ResponseEntity
				.ok()
				.contentType(MediaType.TEXT_EVENT_STREAM)  // 여기서 헤더 명시
				.body(emitter);
	}

	@PatchMapping("/read/{notification-id}")
	@Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
	public ResponseEntity<Void> readNotification(
			@AuthenticationPrincipal CustomOAuth2User principal,
			@Parameter(description = "notification Id", example = "1")
			@PathVariable("notification-id") Long notificationId) {

		Long userId = Long.parseLong(principal.getName());
		notificationService.readNotification(userId, notificationId);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	@Operation(summary = "전체 알림 조회", description = "사용자의 모든 알림을 반환합니다.")
	public ResponseEntity<List<NotificationResponse>> getNotifications(
			@AuthenticationPrincipal CustomOAuth2User principal) {

		Long userId = Long.parseLong(principal.getName());
		return ResponseEntity.ok(notificationService.getNotifications(userId));
	}

	@GetMapping("/recent")
	@Operation(summary = "최근 알림 3건 조회", description = "가장 최근 3개의 알림을 반환합니다.")
	public ResponseEntity<List<NotificationResponse>> getRecentNotifications(
			@AuthenticationPrincipal CustomOAuth2User principal) {

		Long userId = Long.parseLong(principal.getName());
		return ResponseEntity.ok(notificationService.getRecentNotifications(userId));
	}

	@DeleteMapping("/comment/{comment-id}")
	@Operation(summary = "댓글 삭제 시 연관 알림 삭제", description = "커뮤니티 서비스에서 댓글 삭제 후 알림을 정리할 때 호출합니다.")
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
