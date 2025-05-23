package com.playus.userservice.domain.notification.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.playus.userservice.ControllerTestSupport;
import com.playus.userservice.domain.notification.dto.response.NotificationResponse;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.feign.response.CommentNotificationEvent;
import com.playus.userservice.domain.user.feign.response.PartyNotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationControllerTest extends ControllerTestSupport {

    private UsernamePasswordAuthenticationToken token;

    @BeforeEach
    void setUpAuthentication() {
        Long userId = 1L;

        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        given(principal.getName()).willReturn(userId.toString());
        doReturn(List.of(new SimpleGrantedAuthority(Role.USER.name())))
                .when(principal).getAuthorities();

        token = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    @DisplayName("SSE 구독 성공")
    void subscribe_ShouldReturnSseEmitter() throws Exception {
        // 1) 테스트용 Emitter 준비
        SseEmitter emitter = new SseEmitter();
        given(notificationService.subscribe(anyLong(), anyString()))
                .willReturn(emitter);

        // 2) 요청 → asyncStarted() 확인 후 MvcResult 확보
        var mvcResult = mockMvc.perform(get("/user/connect")
                        .with(authentication(token))
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        // 3) 테스트 스레드에서 임의 이벤트 전송 & complete
        emitter.send(SseEmitter.event().data("ping"));
        emitter.complete();

        // 4) asyncDispatch 로 최종 응답 검증
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.TEXT_EVENT_STREAM_VALUE));
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void readNotification_ShouldMarkAsRead() throws Exception {
        // given
        Long notificationId = 1L;

        mockMvc.perform(patch("/user/read/{notification-id}", notificationId)
                        .with(authentication(token)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        then(notificationService).should()
                .readNotification(1L, notificationId);
    }

    @Test
    @DisplayName("전체 알림 목록 조회 성공")
    void getNotifications_ShouldReturnList() throws Exception {
        List<NotificationResponse> stubList = List.of();   // 내용 없는 더미 리스트
        given(notificationService.getNotifications(anyLong())).willReturn(stubList);

        mockMvc.perform(get("/user")
                        .with(authentication(token)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("최근 알림 3건 조회 성공")
    void getRecentNotifications_ShouldReturnRecentList() throws Exception {
        List<NotificationResponse> stubList = List.of();
        given(notificationService.getRecentNotifications(anyLong())).willReturn(stubList);

        mockMvc.perform(get("/user/recent")
                        .with(authentication(token)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("커뮤니티 댓글 알림 생성 요청 성공")
    void createCommentNotification_ShouldReturnCreated() throws Exception {
        // given
        CommentNotificationEvent event = CommentNotificationEvent.of(
                100L,  // commentId
                200L,  // postId
                1L,    // writerId
                "새 댓글이 등록되었습니다.", // content
                true   // activated
        );

        // when & then
        mockMvc.perform(post("/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated());

        then(notificationService).should()
                .sendCommentNotification(any(CommentNotificationEvent.class));
    }

    @Test
    @DisplayName("직관팟 파티 알림 생성 요청 성공")
    void createPartyNotification_ShouldReturnCreated() throws Exception {
        // given
        PartyNotificationEvent event = PartyNotificationEvent.request(
                300L,                  // partyId
                "직관팟 제목",            // partyTitle
                2L,                    // receiverId
                3L,                    // applicantId (actorId)
                "WAIT",              // requestStatus
                "참여 요청합니다."      // requireMessage
        );

        // when & then
        mockMvc.perform(post("/party")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated());

        then(notificationService).should()
                .createPartyNotification(any(PartyNotificationEvent.class));
    }

}
