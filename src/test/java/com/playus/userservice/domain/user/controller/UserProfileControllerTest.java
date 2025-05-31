package com.playus.userservice.domain.user.controller;

import com.playus.userservice.ControllerTestSupport;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.profile.FavoriteTeamDto;
import com.playus.userservice.domain.user.dto.profile.UserProfileResponse;
import com.playus.userservice.domain.user.dto.profile.UserPublicProfileResponse;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserProfileControllerTest extends ControllerTestSupport {

    private UsernamePasswordAuthenticationToken token;

    @BeforeEach
    void setUp() {
        Long userId = 18L;
        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        Mockito.when(principal.getName()).thenReturn(userId.toString());

        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(Role.USER.name()));
        Mockito.doReturn(authorities).when(principal).getAuthorities();

        token = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    @DisplayName("사용자 프로필을 정상적으로 조회할 수 있다.")
    @Test
    void getProfile_success() throws Exception {
        // given
        var favoriteTeams = List.of(
                FavoriteTeamDto.builder().teamId(8L).displayOrder(1).build(),
                FavoriteTeamDto.builder().teamId(1L).displayOrder(2).build()
        );

        var resp = UserProfileResponse.builder()
                .id(18L)
                .nickname("default_nickname")
                .phoneNumber("+821079070479")
                .birth(LocalDate.of(2000, 4, 26))
                .gender(null)
                .role(Role.USER)
                .authProvider(null)
                .activated(true)
                .thumbnailURL("default.png")
                .userScore(0.3f)
                .blockOff(null)
                .favoriteTeams(favoriteTeams)
                .build();

        given(userProfileReadService.getProfile(eq(18L))).willReturn(resp);

        // when // then
        mockMvc.perform(get("/user/profile")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(18))
                .andExpect(jsonPath("$.nickname").value("default_nickname"))
                .andExpect(jsonPath("$.favoriteTeams").isArray())
                .andExpect(jsonPath("$.favoriteTeams[0].teamId").value(8))
                .andExpect(jsonPath("$.favoriteTeams[1].displayOrder").value(2));
    }

    @DisplayName("사용자를 찾을 수 없으면 404 Not Found")
    @Test
    void getProfile_notFound() throws Exception {
        willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                .given(userProfileReadService).getProfile(eq(18L));

        mockMvc.perform(get("/user/profile")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    // 다른 사용자 프로필
    @DisplayName("다른 사람 공개 프로필을 정상적으로 조회할 수 있다.")
    @Test
    void getOtherProfile_success() throws Exception {
        // given
        Long requesterId = 18L;
        Long targetId    = 99L;

        List<FavoriteTeamDto> favoriteTeams = List.of(
                FavoriteTeamDto.builder()
                        .teamId(3L)
                        .displayOrder(1)
                        .build()
        );

        UserPublicProfileResponse resp = UserPublicProfileResponse.builder()
                .id(targetId)
                .nickname("other_user")
                .gender(Gender.FEMALE)
                .thumbnailURL("other.png")
                .userScore(2.5f)
                .favoriteTeams(favoriteTeams)
                .build();

        given(userProfileReadService.getPublicProfile(eq(requesterId), eq(targetId)))
                .willReturn(resp);

        // when/then
        mockMvc.perform(get("/user/profile/{user-id}", targetId)
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetId.intValue()))
                .andExpect(jsonPath("$.nickname").value("other_user"))
                .andExpect(jsonPath("$.thumbnailURL").value("other.png"))
                .andExpect(jsonPath("$.favoriteTeams[0].teamId").value(3))
                .andExpect(jsonPath("$.favoriteTeams[0].displayOrder").value(1));
    }
}
