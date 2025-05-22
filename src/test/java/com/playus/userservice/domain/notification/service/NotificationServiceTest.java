package com.playus.userservice.domain.notification.service;

import com.playus.userservice.domain.notification.dto.response.NotificationResponse;
import com.playus.userservice.domain.user.repository.write.NotificationRepository;
import com.playus.userservice.domain.notification.repository.EmitterRepository;
import com.playus.userservice.domain.user.entity.Notification;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.enums.AuthProvider;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.NotificationType;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.feign.client.CommunityFeignClient;
import com.playus.userservice.domain.user.feign.response.CommentInfo;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmitterRepository emitterRepository;

    @Mock
    private CommunityFeignClient communityFeignClient;

    @InjectMocks
    private NotificationService notificationService;

    private User dummyUser;

    @BeforeEach
    void setup() {
        // 공통적으로 사용할 더미 유저
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
    @DisplayName("SSE 구독: 신규 emitter 생성 후 리턴")
    void subscribe_createsAndReturnsEmitter() {
        // given
        String lastEventId = "";
        SseEmitter emitter = new SseEmitter();
        given(emitterRepository.save(anyString(), any(SseEmitter.class))).willReturn(emitter);

        // when
        SseEmitter result = notificationService.subscribe(1L, lastEventId);

        // then
        assertThat(result).isSameAs(emitter);
        then(emitterRepository).should()
                .save(argThat(id -> id.startsWith("1_")), any(SseEmitter.class));
    }

    @Test
    @DisplayName("댓글 알림 발송: 활성화된 댓글이면 저장 후 dispatch")
    void sendCommentNotification_success() {
        // given
        Long commentId = 42L, postId = 99L;
        CommentInfo info = CommentInfo.of(
                commentId, postId, dummyUser.getId(), null, "hello", true
        );
        given(communityFeignClient.getComment(commentId)).willReturn(info);
        given(userRepository.findById(dummyUser.getId()))
                .willReturn(Optional.of(dummyUser));
        given(notificationRepository.save(any(Notification.class)))
                .willAnswer(inv -> inv.getArgument(0));
        given(emitterRepository.findAllEmitterStartWithByUserId("1"))
                .willReturn(Map.of("1_123", new SseEmitter()));

        // when
        notificationService.sendCommentNotification(commentId);

        then(notificationRepository).should()
                .save(any(Notification.class));
        then(emitterRepository).should()
                .saveEventCache(eq("1_123"), any(Notification.class));
    }

    @Test
    @DisplayName("댓글 알림 발송: 비활성 댓글이면 무시")
    void sendCommentNotification_inactive_noop() {
        // given
        given(communityFeignClient.getComment(5L))
                .willReturn(CommentInfo.of(5L, null, null, null, "", false));

        // when / then
        // 예외 없이 그냥 리턴
        notificationService.sendCommentNotification(5L);
        then(notificationRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("읽음 처리: 존재하지 않는 알림이면 404")
    void readNotification_notFound_throws() {
        // given
        given(notificationRepository.findById(99L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> notificationService.readNotification(1L, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("읽음 처리: 소유자가 다르면 400")
    void readNotification_forbidden_throws() {
        // given
        Notification n = Notification.create(dummyUser, "테스트 댓글", "a", null,null, null, NotificationType.COMMENT);
        ReflectionTestUtils.setField(n, "id", 5L);
        given(notificationRepository.findById(5L)).willReturn(Optional.of(n));

        // when / then
        assertThatThrownBy(() -> notificationService.readNotification(2L, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    @DisplayName("읽음 처리: 정상 호출이면 isRead=true")
    void readNotification_success() {
        // given
        Notification n = Notification.create(dummyUser, "테스트 댓글", "b", null,null, null, NotificationType.COMMENT);
        ReflectionTestUtils.setField(n, "id", 7L);
        given(notificationRepository.findById(7L)).willReturn(Optional.of(n));

        // when
        notificationService.readNotification(1L, 7L);

        // then
        assertThat(n.isRead()).isTrue();
    }

    @Test
    @DisplayName("전체 조회: 존재하지 않는 유저면 404")
    void getNotifications_userNotFound_throws() {
        // given
        given(userRepository.findById(2L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> notificationService.getNotifications(2L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("전체 조회: 정상 호출이면 DTO 리스트 리턴")
    void getNotifications_success() {
        // given
        Notification n = Notification.create(dummyUser, "테스트 댓글", "c", null,null, null, NotificationType.COMMENT);
        given(userRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(notificationRepository.findAllByReceiver(dummyUser))
                .willReturn(List.of(n));

        // when
        var list = notificationService.getNotifications(1L);

        // then
        assertThat(list).hasSize(1)
                .first()
                .usingRecursiveComparison()
                .isEqualTo(NotificationResponse.from(n));
    }

    @Test
    @DisplayName("최신 3건 조회: 정상 호출이면 최대 3개")
    void getRecentNotifications_success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        List<Notification> four = List.of(
                // 이제 7개 인자를 모두 넘겨야 합니다.
                Notification.create(dummyUser,
                        "테스트 댓글",  // title
                        "a",           // content
                        null,          // commentId
                        null,          // partyId
                        null,          // actorId
                        NotificationType.COMMENT),
                Notification.create(dummyUser, "테스트 댓글", "b", null, null, null, NotificationType.COMMENT),
                Notification.create(dummyUser, "테스트 댓글", "c", null, null, null, NotificationType.COMMENT),
                Notification.create(dummyUser, "테스트 댓글", "d", null, null, null, NotificationType.COMMENT)
        );
        given(notificationRepository.findTop3ByReceiverOrderByCreatedAtDesc(dummyUser))
                .willReturn(four.subList(0, 3));

        // when
        var list = notificationService.getRecentNotifications(1L);

        // then
        assertThat(list).hasSize(3);
    }

    @Test
    @DisplayName("댓글 ID로 삭제: 정상 호출이면 repository.deleteAllByCommentId 호출")
    void deleteByCommentId_success() {
        // when
        notificationService.deleteByCommentId(55L);

        // then
        then(notificationRepository).should().deleteAllByCommentId(55L);
    }
}
