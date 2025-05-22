package com.playus.userservice.domain.notification.service;

import com.playus.userservice.domain.notification.dto.request.NoticeRequest;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.playus.userservice.domain.notification.dto.response.NotificationResponse;
import com.playus.userservice.domain.notification.repository.EmitterRepository;
import com.playus.userservice.domain.notification.repository.NotificationRepository;
import com.playus.userservice.domain.user.entity.Notification;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.enums.NotificationType;
import com.playus.userservice.domain.user.feign.client.CommunityFeignClient;
import com.playus.userservice.domain.user.feign.response.CommentInfo;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	// 1시간
	private static final Long DEFAULT_TIMEOUT = 60L * 60 * 1000;

	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;
	private final EmitterRepository emitterRepository;
	private final CommunityFeignClient communityFeignClient;

	public NotificationService(
			UserRepository userRepository,
			NotificationRepository notificationRepository,
			EmitterRepository emitterRepository,
			@Qualifier("communityFeignClient") CommunityFeignClient communityFeignClient
	) {
		this.userRepository       = userRepository;
		this.notificationRepository = notificationRepository;
		this.emitterRepository    = emitterRepository;
		this.communityFeignClient = communityFeignClient;
	}


	/**
	 * SSE 구독 (CONNECT)
	 */
	public SseEmitter subscribe(Long userId, String lastEventId) {
		String emitterId = userId + "_" + System.currentTimeMillis();
		SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

		// 자동 정리
		emitter.onCompletion(() -> cleanup(emitterId));
		emitter.onTimeout(   () -> cleanup(emitterId));

		// 더미 이벤트 전송 (브라우저 503 대응)
		send(emitter, emitterId, "EventStream Created. [userId=" + userId + "]");

		// 끊겼던 ID 이후 이벤트 재전송
		if (lastEventId != null && !lastEventId.isEmpty()) {
			Map<String, Object> cache = emitterRepository
					.findAllEventCacheStartWithByUserId(userId.toString());
			cache.entrySet().stream()
					.filter(e -> e.getKey().compareTo(lastEventId) > 0)
					.forEach(e -> send(emitter, e.getKey(), e.getValue()));
		}

		return emitter;
	}

	/**
	 * 댓글 알림 발송
	 */
	@Transactional
	public void sendCommentNotification(Long commentId) {
		// 1) Feign 호출
		CommentInfo info = communityFeignClient.getComment(commentId);
		if (info == null || !info.activated()) {
			return; // 비활성 댓글 혹은 장애 시 무시
		}

		// 2) 알림 수신자 = 게시글 작성자(userId)
		User receiver = userRepository.findById(info.writerId())
				.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		// 3) Notification 생성
		String title   = String.format("내가 쓴 글에 댓글이 달렸어요: %s", info.content());
		String content = String.format("제목: %s", info.content());
		Notification notification = notificationRepository.save(
				Notification.create(receiver, commentId, content, NotificationType.COMMENT)
		);

		// 4) SSE 전송
		dispatchToClient(receiver.getId(), notification, info.postId());
	}

	/**
	 * 공지(관리자) 발송
	 */
	@Transactional
	public void sendNotice(NoticeRequest request) {
		List<Notification> notis = userRepository.findAll().stream()
				.map(u -> Notification.create(u, null, request.content(), NotificationType.NOTICE))
				.map(notificationRepository::save)
				.collect(Collectors.toList());

		notis.forEach(n -> dispatchToClient(n.getReceiver().getId(), n, null));
	}

	/**
	 * 특정 유저의 모든 알림 삭제 + SSE 연결 종료
	 */
	@Transactional
	public void delete(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
		emitterRepository.deleteAllEmitterStartWithId(userId.toString());
		emitterRepository.deleteAllEventCacheStartWithId(userId.toString());
		notificationRepository.deleteAllByReceiver(user);
	}

	/**
	 * 알림 읽음 처리
	 */
	@Transactional
	public void readNotification(Long userId, Long notificationId) {
		Notification n = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));
		if (!n.getReceiver().getId().equals(userId)) {
			throw new BadRequestException(ErrorCode.NOTIFICATION_READ_FORBIDDEN);
		}
		n.markAsRead();
	}

	/**
	 * 전체 알림 조회
	 */
	@Transactional
	public List<NotificationResponse> getNotifications(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
		return notificationRepository.findAllByReceiver(user).stream()
				.map(n -> NotificationResponse.from(n, null))
				.collect(Collectors.toList());
	}

	/**
	 * 최근 3개 알림 조회
	 */
	@Transactional
	public List<NotificationResponse> getRecentNotifications(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
		return notificationRepository
				.findTop3ByReceiverOrderByCreatedAtDesc(user).stream()
				.map(n -> NotificationResponse.from(n, null))
				.collect(Collectors.toList());
	}

	/* SSE 전송 보조 메서드 */
	private void dispatchToClient(Long receiverId, Notification notification, Long boardId) {
		String prefix = receiverId.toString();
		Map<String, SseEmitter> emitters = emitterRepository
				.findAllEmitterStartWithByUserId(prefix);

		emitters.forEach((key, emitter) -> {
			emitterRepository.saveEventCache(key, notification);
			if (send(emitter, key, NotificationResponse.from(notification, boardId))) {
				log.warn("알림 전송 실패 - emitterId: {}", key);
			}
		});
	}

	/* Emitter + Cache 정리 */
	private void cleanup(String emitterId) {
		emitterRepository.deleteById(emitterId);
		emitterRepository.deleteAllEventCacheStartWithId(emitterId);
	}

	/* 실제 SSE 전송, 실패 시 정리 및 true 리턴 */
	private boolean send(SseEmitter emitter, String id, Object data) {
		try {
			emitter.send(SseEmitter.event().id(id).data(data));
			return false;
		} catch (IOException e) {
			cleanup(id);
			return true;
		}
	}
}
