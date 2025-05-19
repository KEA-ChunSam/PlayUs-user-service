package com.playus.userservice.domain.user.controller;

import com.playus.userservice.ControllerTestSupport;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.presigned.PresignedUrlForSaveImageRequest;
import com.playus.userservice.domain.user.dto.presigned.PresignedUrlForSaveImageResponse;
import com.playus.userservice.domain.user.dto.profilesetup.UserRegisterRequest;
import com.playus.userservice.domain.user.dto.profilesetup.UserRegisterResponse;
import com.playus.userservice.domain.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfileSetupControllerTest extends ControllerTestSupport {

    private UsernamePasswordAuthenticationToken token;

    @BeforeEach
    void init() {
        Long userId = 1L;
        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        when(principal.getName()).thenReturn(userId.toString());

        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(Role.USER.name()));
        doReturn(authorities).when(principal).getAuthorities();

        token = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    /* ---------- Happy case ---------- */

    @DisplayName("프로필(닉네임,팀,썸네일)을 정상 설정한다.")
    @Test
    void setupProfile() throws Exception {
        UserRegisterRequest req  =
                new UserRegisterRequest(7L, "닉네임", "image.jpg");
        UserRegisterResponse resp =
                new UserRegisterResponse(true, "프로필이 정상적으로 설정되었습니다.");

        given(profileSetupService.setupProfile(any(), any(), any(), any()))
                .willReturn(resp);

        mockMvc.perform(post("/user/register")
                        .content(objectMapper.writeValueAsString(req))
                        .contentType(APPLICATION_JSON)
                        .with(authentication(token)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로필이 정상적으로 설정되었습니다."));
    }

    /* ---------- Validation : teamId / nickname / thumbnailURL ---------- */

    @DisplayName("favoriteTeam(teamId) 필드는 필수이다.")
    @Test
    void setupProfile_EMPTY_TEAM_ID() throws Exception {
        UserRegisterRequest req =
                new UserRegisterRequest(null, "닉네임", "image.jpg");
        assertBadRequest(req, "favoriteTeam 필드는 필수입니다.");
    }

    @DisplayName("nickname 필드는 필수이다.")
    @NullAndEmptySource
    @ParameterizedTest(name = "nickname = \"{0}\"")
    void setupProfile_BLANK_NICKNAME(String blank) throws Exception {
        UserRegisterRequest req =
                new UserRegisterRequest(5L, blank, "image.jpg");
        assertBadRequest(req, "nickname 필드는 필수입니다.");
    }

    @DisplayName("thumbnailURL 필드는 필수이다.")
    @NullAndEmptySource
    @ParameterizedTest(name = "thumbnailURL = \"{0}\"")
    void setupProfile_BLANK_URL(String blank) throws Exception {
        UserRegisterRequest req =
                new UserRegisterRequest(5L, "닉네임", blank);
        assertBadRequest(req, "thumbnailURL 필드는 필수입니다.");
    }

    /* ---------- Presigned URL ---------- */

    @DisplayName("이미지 저장용 Presigned URL을 발급한다.")
    @Test
    void generatePresignedUrl() throws Exception {
        PresignedUrlForSaveImageRequest  req  =
                new PresignedUrlForSaveImageRequest("image.jpg");
        PresignedUrlForSaveImageResponse resp =
                new PresignedUrlForSaveImageResponse("https://pre-signed");

        given(profileSetupService.generatePresignedUrlForSaveImage(any()))
                .willReturn(resp);

        mockMvc.perform(post("/user/presigned-url")
                        .content(objectMapper.writeValueAsString(req))
                        .contentType(APPLICATION_JSON)
                        .with(authentication(token)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presignedUrl").value("https://pre-signed"));
    }

    @DisplayName("URL 발급 시 이미지 파일명은 필수이다.")
    @NullAndEmptySource
    @ParameterizedTest
    void generatePresignedUrl_BLANK_NAME(String blank) throws Exception {
        PresignedUrlForSaveImageRequest req =
                new PresignedUrlForSaveImageRequest(blank);

        mockMvc.perform(post("/user/presigned-url")
                        .content(objectMapper.writeValueAsString(req))
                        .contentType(APPLICATION_JSON)
                        .with(authentication(token)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미지 파일명은 필수입니다!"));
    }

    /* ---------- 공용 검증 메서드 ---------- */

    private void assertBadRequest(UserRegisterRequest req, String expectedMsg) throws Exception {
        mockMvc.perform(post("/user/register")
                        .content(objectMapper.writeValueAsString(req))
                        .contentType(APPLICATION_JSON)
                        .with(authentication(token)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(expectedMsg));
    }
}
