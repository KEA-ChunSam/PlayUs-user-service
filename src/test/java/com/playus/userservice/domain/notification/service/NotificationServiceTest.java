package com.playus.userservice.domain.notification.service;

import com.playus.userservice.domain.notification.dto.response.NotificationResponse;
import com.playus.userservice.domain.notification.repository.EmitterRepository;
import com.playus.userservice.domain.user.entity.Notification;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.enums.*;
import com.playus.userservice.domain.user.feign.response.CommentNotificationEvent;
import com.playus.userservice.domain.user.feign.response.PartyNotificationEvent;
import com.playus.userservice.domain.user.repository.write.NotificationRepository;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private UserRepository          userRepository;
    @Mock private NotificationRepository  notificationRepository;
    @Mock private EmitterRepository       emitterRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User dummyUser;

    @BeforeEach
    void setup() {
        dummyUser = User.create(
                "dummy@example.com",
                "010-0000-0000",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                Role.USER,
                AuthProvider.KAKAO,
                "http://thumb"
        );
        ReflectionTestUtils.setField(dummyUser, "id", 1L);
    }

    @Test
    @DisplayName("SSE 구독: 신규 emitter 생성 후 반환")
    void subscribe_createsAndReturnsEmitter() {
        // given
        SseEmitter emitter = new SseEmitter();
        given(emitterRepository.save(anyString(), any(SseEmitter.class))).willReturn(emitter);

        // when
        SseEmitter result = notificationService.subscribe(1L, "");

        // then
        assertThat(result).isSameAs(emitter);
        then(emitterRepository).should()
                .save(argThat(id -> id.startsWith("1_")), any(SseEmitter.class));
    }

    @Test
    @DisplayName("댓글 알림: 활성화된 이벤트면 저장 · dispatch")
    void sendCommentNotification_success() {
        // given
        CommentNotificationEvent event = CommentNotificationEvent.of(
                42L, 99L, dummyUser.getId(), "", true
        );

        given(userRepository.findById(dummyUser.getId()))
                .willReturn(Optional.of(dummyUser));
        given(notificationRepository.save(any(Notification.class)))
                .willAnswer(inv -> inv.getArgument(0));
        given(emitterRepository.findAllEmitterStartWithByUserId("1"))
                .willReturn(Map.of("1_123", new SseEmitter()));

        // when
        notificationService.sendCommentNotification(event);

        // then
        then(notificationRepository).should().save(any(Notification.class));
        then(emitterRepository).should()
                .saveEventCache(eq("1_123"), any(Notification.class));
    }

    @Test
    @DisplayName("댓글 알림: 비활성 이벤트면 아무 일도 하지 않음")
    void sendCommentNotification_inactive_noop() {
        // given
        CommentNotificationEvent event = CommentNotificationEvent.of(
                5L, 1L, 1L, "", false
        );
        // when
        notificationService.sendCommentNotification(event);

        // then
        then(notificationRepository).shouldHaveNoInteractions();
        then(emitterRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("읽음 처리: 존재하지 않는 알림이면 404")
    void readNotification_notFound_throws() {
        given(notificationRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.readNotification(1L, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("읽음 처리: 소유자가 다르면 400")
    void readNotification_forbidden_throws() {
        Notification n = Notification.create(dummyUser, "title", "content",
                null, null, null, NotificationType.COMMENT);
        ReflectionTestUtils.setField(n, "id", 5L);
        given(notificationRepository.findById(5L)).willReturn(Optional.of(n));

        assertThatThrownBy(() -> notificationService.readNotification(2L, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    @DisplayName("읽음 처리: 정상 호출이면 isRead == true")
    void readNotification_success() {
        Notification n = Notification.create(dummyUser, "title", "content",
                null, null, null, NotificationType.COMMENT);
        ReflectionTestUtils.setField(n, "id", 7L);
        given(notificationRepository.findById(7L)).willReturn(Optional.of(n));

        notificationService.readNotification(1L, 7L);

        assertThat(n.isRead()).isTrue();
    }

    @Test
    @DisplayName("전체 조회: 유저가 없으면 404")
    void getNotifications_userNotFound_throws() {
        given(userRepository.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotifications(2L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("전체 조회: 정상 호출")
    void getNotifications_success() {
        Notification n = Notification.create(dummyUser, "t", "c",
                null, null, null, NotificationType.COMMENT);
        given(userRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(notificationRepository.findAllByReceiver(dummyUser))
                .willReturn(List.of(n));

        List<NotificationResponse> list = notificationService.getNotifications(1L);

        assertThat(list).containsExactly(NotificationResponse.from(n));
    }

    @Test
    @DisplayName("최근 3건 조회: 최대 3개 반환")
    void getRecentNotifications_success() {
        List<Notification> four = List.of(
                Notification.create(dummyUser, "t1", "a", null, null, null, NotificationType.COMMENT),
                Notification.create(dummyUser, "t2", "b", null, null, null, NotificationType.COMMENT),
                Notification.create(dummyUser, "t3", "c", null, null, null, NotificationType.COMMENT),
                Notification.create(dummyUser, "t4", "d", null, null, null, NotificationType.COMMENT)
        );
        given(userRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(notificationRepository.findTop3ByReceiverOrderByCreatedAtDesc(dummyUser))
                .willReturn(four.subList(0, 3));

        List<NotificationResponse> list = notificationService.getRecentNotifications(1L);

        assertThat(list).hasSize(3);
    }

    @Test
    @DisplayName("댓글 ID 삭제: repository 호출")
    void deleteByCommentId_success() {
        notificationService.deleteByCommentId(55L);
        then(notificationRepository).should().deleteAllByCommentId(55L);
    }

    @Test
    @DisplayName("파티 알림: 신청 요청 저장 · dispatch")
    void createPartyNotification_request_success() {
        // given
        PartyNotificationEvent event = PartyNotificationEvent.request(
                300L,   // partyId
                "직관팟",  // title
                dummyUser.getId(),  // receiver (작성자)
                99L,    // applicantId (actor)
                "WAIT", // 상태
                null    // 메시지
        );

        given(userRepository.findById(dummyUser.getId())).willReturn(Optional.of(dummyUser));
        given(notificationRepository.save(any(Notification.class)))
                .willAnswer(inv -> inv.getArgument(0));
        given(emitterRepository.findAllEmitterStartWithByUserId("1"))
                .willReturn(Map.of("1_456", new SseEmitter()));

        // when
        notificationService.createPartyNotification(event);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        then(notificationRepository).should().save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getPartyId()).isEqualTo(300L);
        assertThat(saved.getType()).isEqualTo(NotificationType.PARTY_REQUEST);
        assertThat(saved.getTitle()).contains("새 참가 요청이 도착했습니다");
        assertThat(saved.getContent()).contains("-");

        then(emitterRepository).should().saveEventCache(eq("1_456"), any(Notification.class));
    }
}
