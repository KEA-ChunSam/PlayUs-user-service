package com.playus.userservice.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.ProfileSetupDto.UserRegisterRequest;
import com.playus.userservice.domain.user.dto.ProfileSetupDto.UserRegisterResponse;
import com.playus.userservice.domain.user.dto.UserDto;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.service.ProfileSetupService;
import com.playus.userservice.global.exception.ExceptionAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProfileSetupControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProfileSetupService setupService;

    @BeforeEach
    void setup() {
        var controller = new ProfileSetupController(setupService);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ExceptionAdvice())
                .build();
    }

    private UsernamePasswordAuthenticationToken token() {
        CustomOAuth2User principal =
                new CustomOAuth2User(UserDto.fromJwt(1L, Role.USER));
        return new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
    }

    @DisplayName("성공: 닉네임과 선호팀을 함께 설정")
    @Test
    void register_Success() throws Exception {
        UserRegisterRequest req = new UserRegisterRequest(8L, "newbie");
        UserRegisterResponse respDto =
                new UserRegisterResponse(true, "프로필이 정상적으로 설정되었습니다.");

        given(setupService.setupProfile(eq(1L), eq(8L), eq("newbie")))
                .willReturn(respDto);

        mockMvc.perform(post("/user/register")
                        .with(authentication(token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("프로필이 정상적으로 설정되었습니다."));
    }

    @DisplayName("실패: 닉네임이 비어 있거나 null")
    @ParameterizedTest(name = "nickname=\"{0}\"")
    @NullAndEmptySource
    void register_InvalidNickname(String nickname) throws Exception {
        UserRegisterRequest req = new UserRegisterRequest(8L, nickname);
        mockMvc.perform(post("/user/register")
                        .with(authentication(token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("실패: 선호팀을 선택하지 않은 경우")
    @Test
    void register_MissingFavoriteTeam() throws Exception {
        // omit teamId entirely
        String body = "{\"nickname\":\"star\"}";
        mockMvc.perform(post("/user/register")
                        .with(authentication(token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("실패: 닉네임 중복 시도")
    @Test
    void register_DuplicateNickname() throws Exception {
        UserRegisterRequest req = new UserRegisterRequest(3L, "duplicate");
        given(setupService.setupProfile(eq(1L), eq(3L), eq("duplicate")))
                .willThrow(new RuntimeException("USER_ALREADY_EXISTS"));

        mockMvc.perform(post("/user/register")
                        .with(authentication(token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }
}