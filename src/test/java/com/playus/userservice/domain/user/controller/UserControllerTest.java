package com.playus.userservice.domain.user.controller;

import com.playus.userservice.ControllerTestSupport;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.nickname.NicknameRequest;
import com.playus.userservice.domain.user.dto.nickname.NicknameResponse;
import com.playus.userservice.domain.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends ControllerTestSupport {

    private UsernamePasswordAuthenticationToken token;

    @BeforeEach
    void setUp() {
        Long userId = 1L;

        // 더미 OAuth2 사용자
        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        when(principal.getName()).thenReturn(userId.toString());

        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(Role.USER.name()));
        doReturn(authorities).when(principal).getAuthorities();

        token = new UsernamePasswordAuthenticationToken(
                principal, null, authorities
        );
    }

    @DisplayName("닉네임을 정상적으로 변경할 수 있다.")
    @Test
    void updateNickname_success() throws Exception {
        // given
        var req = new NicknameRequest("newNick");
        var resp = new NicknameResponse(true, "닉네임이 성공적으로 변경되었습니다.");
        given(userService.updateNickname(eq(1L), any(NicknameRequest.class)))
                .willReturn(resp);

        // when // then
        mockMvc.perform(put("/user/nickname")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("닉네임이 성공적으로 변경되었습니다."));
    }

    @DisplayName("중복 닉네임은 409 Conflict")
    @Test
    void updateNickname_conflict() throws Exception {
        var req = new NicknameRequest("dupNick");
        willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."))
                .given(userService).updateNickname(eq(1L), any(NicknameRequest.class));

        mockMvc.perform(put("/user/nickname")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("409"))
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value("이미 사용 중인 닉네임입니다."));
    }

    @DisplayName("사용자가 없으면 404 Not Found")
    @Test
    void updateNickname_notFound() throws Exception {
        var req = new NicknameRequest("anyNick");
        willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                .given(userService).updateNickname(eq(1L), any(NicknameRequest.class));

        mockMvc.perform(put("/user/nickname")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("사용자를 찾을 수 없습니다."));
    }

    @DisplayName("nickname 필드는 필수이다.")
    @ParameterizedTest
    @NullAndEmptySource
    void updateNickname_blankNickname(String blank) throws Exception {
        var req = new NicknameRequest(blank);

        mockMvc.perform(put("/user/nickname")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message")
                        .value("nickname 필드는 필수입니다."));
    }
}
