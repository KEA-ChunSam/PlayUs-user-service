package com.playus.userservice.domain.user.controller;

import com.playus.userservice.ControllerTestSupport;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.UserReviewRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserReviewControllerTest extends ControllerTestSupport {

    private UsernamePasswordAuthenticationToken token;

    @BeforeEach
    void setUp() {
        Long reviewerId = 1L;

        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        Mockito.when(principal.getName()).thenReturn(reviewerId.toString());

        var authorities = List.of(new SimpleGrantedAuthority(Role.USER.name()));
        token = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    @DisplayName("리뷰 요청이 200 OK 로 반환된다")
    @Test
    void createUserReviews_success() throws Exception {
        // given
        List<UserReviewRequest> reqs = List.of(
                new UserReviewRequest(2L, 3L, true),
                new UserReviewRequest(2L, 4L, false)
        );

        given(userReviewService.processReviews(eq(1L), any()))
                .willReturn(reqs);

        // when // then
        mockMvc.perform(post("/users/reviews")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqs)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(2))
                .andExpect(jsonPath("$[0].tagId").value(3))
                .andExpect(jsonPath("$[0].positive").value(true))
                .andExpect(jsonPath("$[1].positive").value(false));
    }

    @DisplayName("service 가 예외를 던지면 동일한 상태 코드로 응답한다")
    @Test
    void createUserReviews_serviceThrows() throws Exception {
        // given
        List<UserReviewRequest> reqs = List.of(
                new UserReviewRequest(999L, 3L, true)
        );

        willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰 대상 유저가 존재하지 않습니다."))
                .given(userReviewService).processReviews(eq(1L), any());

        // when // then
        mockMvc.perform(post("/users/reviews")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqs)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("리뷰 대상 유저가 존재하지 않습니다."));
    }
}
