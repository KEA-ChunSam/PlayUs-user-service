package com.playus.userservice.domain.user.controller;

import com.playus.userservice.ControllerTestSupport;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.UserTagSummaryResponse;
import com.playus.userservice.domain.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserTagReadControllerTest extends ControllerTestSupport {

    private UsernamePasswordAuthenticationToken token;
    private CustomOAuth2User principal;

    @BeforeEach
    void setUp() {
        Long userId = 1L;
        principal = Mockito.mock(CustomOAuth2User.class);
        Mockito.when(principal.getName()).thenReturn(userId.toString());

        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(Role.USER.name()));
        Mockito.doReturn(authorities).when(principal).getAuthorities();

        token = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    @DisplayName("유저 태그 요약을 정상적으로 조회한다")
    @Test
    void getUserTagSummary_success() throws Exception {
        Long userId = 1L;
        Long targetId = 7L;
        var resp = new UserTagSummaryResponse(
                5L,
                4L,
                List.of(
                        "시간 약속을 잘 지켜요.",
                        "경기 직관이 열정적이에요.",
                        "상대방에 대한 배려심이 깊어요."
                )
        );
        given(userTagReadService.getUserTagSummary(eq(userId), eq(targetId)))
                .willReturn(resp);

        mockMvc.perform(get("/users/{user-Id}/tags/summary", targetId)
                        .with(oauth2Login().oauth2User(principal))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(5))
                .andExpect(jsonPath("$.positiveTagCount").value(4))
                .andExpect(jsonPath("$.topTags[0]").value("시간 약속을 잘 지켜요."));
    }

    @DisplayName("조회자를 찾을 수 없으면 404")
    @Test
    void getUserTagSummary_viewerNotFound() throws Exception {
        Long userId = 1L;
        Long targetId = 7L;
        willThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                .given(userTagReadService)
                .getUserTagSummary(eq(userId), eq(targetId));

        mockMvc.perform(get("/users/{user-Id}/tags/summary", targetId)
                        .with(oauth2Login().oauth2User(principal))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    @DisplayName("조회 대상 사용자를 찾을 수 없으면 404")
    @Test
    void getUserTagSummary_targetNotFound() throws Exception {
        Long userId = 1L;
        Long targetId = 999L;
        willThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND, "조회하려는 사용자를 찾을 수 없습니다."))
                .given(userTagReadService)
                .getUserTagSummary(eq(userId), eq(targetId));

        mockMvc.perform(get("/users/{user-Id}/tags/summary", targetId)
                        .with(oauth2Login().oauth2User(principal))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("조회하려는 사용자를 찾을 수 없습니다."));
    }
}
