package com.playus.userservice.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.FavoriteTeamDto.FavoriteTeamRequest;
import com.playus.userservice.domain.user.dto.FavoriteTeamDto.FavoriteTeamResponse;
import com.playus.userservice.domain.user.service.FavoriteTeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class FavoriteTeamControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FavoriteTeamService favoriteTeamService;

    // ★ CustomOAuth2User 도 MockBean 이 아닌 @Mock 으로 선언
    @Mock
    private CustomOAuth2User principal;

    @BeforeEach
    void setup() {
        // principal.getName() → "1", getAuthorities() → ROLE_USER
        when(principal.getName()).thenReturn("1");
        when(principal.getAuthorities())
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        FavoriteTeamController controller =
                new FavoriteTeamController(favoriteTeamService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    private UsernamePasswordAuthenticationToken token() {
        // stubbed principal 을 그대로 넣어줍니다.
        return new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
    }

    @DisplayName("POST /user/favorite-teams 성공")
    @Test
    void saveOrUpdateOne_Success() throws Exception {
        var request = new FavoriteTeamRequest(8L, 1);
        var respDto = new FavoriteTeamResponse(true, "선호팀 등록");

        given(favoriteTeamService.setFavoriteTeam(
                eq(1L), any(FavoriteTeamRequest.class)))
                .willReturn(respDto);

        mockMvc.perform(post("/user/favorite-teams")
                        .with(authentication(token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("선호팀 등록"));
    }

    @DisplayName("PUT /user/favorite-teams 성공")
    @Test
    void updateMany_Success() throws Exception {
        var requests = List.of(
                new FavoriteTeamRequest(2L, 1),
                new FavoriteTeamRequest(5L, 2)
        );
        var respDto = new FavoriteTeamResponse(true, "목록 저장");

        given(favoriteTeamService.updateFavoriteTeams(
                eq(1L), anyList()))
                .willReturn(respDto);

        mockMvc.perform(put("/user/favorite-teams")
                        .with(authentication(token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("목록 저장"));
    }
}
