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

        SseEmitter emitter = new SseEmitter();
        given(notificationService.subscribe(anyLong(), anyString()))
                .willReturn(emitter);

        var mvcResult = mockMvc.perform(get("/user/notifications/connect")
                        .with(authentication(token))
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        emitter.send(SseEmitter.event().data("ping"));
        emitter.complete();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.TEXT_EVENT_STREAM_VALUE));
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void readNotification_ShouldMarkAsRead() throws Exception {
        // given
        Long notificationId = 1L;

        mockMvc.perform(patch("/user/notifications/read/{notification-id}", notificationId)
                        .with(authentication(token)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        then(notificationService).should()
                .readNotification(1L, notificationId);
    }

    @Test
    @DisplayName("전체 알림 목록 조회 성공")
    void getNotifications_ShouldReturnList() throws Exception {
        List<NotificationResponse> stubList = List.of();
        given(notificationService.getNotifications(anyLong())).willReturn(stubList);

        mockMvc.perform(get("/user/notifications")
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

        mockMvc.perform(get("/user/notifications/recent")
                        .with(authentication(token)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

}
