package com.playus.userservice.domain.notification.service;

import com.playus.userservice.domain.notification.dto.request.NoticeRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	/** 1시간 (밀리초) */
	private static final Long DEFAULT_TIMEOUT = 60L * 60 * 1000;

	private final UserRepository          userRepository;
	private final NotificationRepository  notificationRepository;
	private final EmitterRepository       emitterRepository;
	private final CommunityFeignClient    communityFeignClient;

	/* ===============================  SSE 연결  =============================== */

	/** SSE 구독 (CONNECT) */
	public SseEmitter subscribe(Long userId, String lastEventId) {
		String emitterId = userId + "_" + System.currentTimeMillis();
		SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

		// 자동 정리
		emitter.onCompletion(() -> cleanup(emitterId));
		emitter.onTimeout   (() -> cleanup(emitterId));

		// 더미 이벤트 전송 (브라우저 503 대응)
		send(emitter, emitterId, "EventStream Created. [userId=" + userId + "]");

		// 끊겼던 ID 이후 이벤트 재전송
		if (lastEventId != null && !lastEventId.isEmpty()) {
			Map<String, Object> cache = emitterRepository.findAllEventCacheStartWithByUserId(userId.toString());
			cache.entrySet().stream()
					.filter(e -> e.getKey().compareTo(lastEventId) > 0)
					.forEach(e -> send(emitter, e.getKey(), e.getValue()));
		}

		return emitter;
	}

	/* ============================  알림 비즈니스  ============================ */

	/** 댓글 알림 발송 */
	@Transactional
	public void sendCommentNotification(Long commentId) {
		CommentInfo info = communityFeignClient.getComment(commentId);
		if (info == null || !info.activated()) {
			return; // 비활성 댓글 · 장애 시 무시
		}

		User receiver = userRepository.findById(info.writerId())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

		Notification notification = notificationRepository.save(
				Notification.create(
						receiver,
						commentId,
						String.format("제목: %s", info.content()),
						NotificationType.COMMENT
				)
		);

		dispatchToClient(receiver.getId(), notification, info.postId());
	}


	/** 특정 유저의 모든 알림 삭제 + SSE 연결 종료 */
	@Transactional
	public void delete(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
		emitterRepository.deleteAllEmitterStartWithId(userId.toString());
		emitterRepository.deleteAllEventCacheStartWithId(userId.toString());
		notificationRepository.deleteAllByReceiver(user);
	}

	/** 알림 읽음 처리 */
	@Transactional
	public void readNotification(Long userId, Long notificationId) {
		Notification n = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));

		if (!n.getReceiver().getId().equals(userId)) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "해당 알림을 읽을 수 없습니다.");
		}
		n.markAsRead();
	}

	/** 전체 알림 조회 */
	@Transactional
	public List<NotificationResponse> getNotifications(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

		return notificationRepository.findAllByReceiver(user).stream()
				.map(n -> NotificationResponse.from(n, null))
				.collect(Collectors.toList());
	}

	/** 최근 3개 알림 조회 */
	@Transactional
	public List<NotificationResponse> getRecentNotifications(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

		return notificationRepository.findTop3ByReceiverOrderByCreatedAtDesc(user).stream()
				.map(n -> NotificationResponse.from(n, null))
				.collect(Collectors.toList());
	}

	@Transactional
	public void deleteByCommentId(Long commentId) {
		notificationRepository.deleteAllByCommentId(commentId);
	}



	private void dispatchToClient(Long receiverId, Notification notification, Long boardId) {
		String prefix = receiverId.toString();
		Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByUserId(prefix);

		emitters.forEach((key, emitter) -> {
			emitterRepository.saveEventCache(key, notification);
			if (send(emitter, key, NotificationResponse.from(notification, boardId))) {
				log.warn("알림 전송 실패 - emitterId: {}", key);
			}
		});
	}

	private void cleanup(String emitterId) {
		emitterRepository.deleteById(emitterId);
		emitterRepository.deleteAllEventCacheStartWithId(emitterId);
	}

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
