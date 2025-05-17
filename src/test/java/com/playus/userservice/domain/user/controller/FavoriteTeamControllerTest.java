package com.playus.userservice.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playus.userservice.ControllerTestSupport;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.FavoriteTeamDto;
import com.playus.userservice.domain.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FavoriteTeamControllerTest extends ControllerTestSupport {

    @Autowired
    private ObjectMapper objectMapper;
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

    @DisplayName("단일 선호팀을 정상적으로 등록/수정할 수 있다.")
    @Test
    void saveOrUpdateOne_success() throws Exception {
        // given
        FavoriteTeamDto.FavoriteTeamRequest req = new FavoriteTeamDto.FavoriteTeamRequest(7L, 1);
        FavoriteTeamDto.FavoriteTeamResponse resp = new FavoriteTeamDto.FavoriteTeamResponse(
                true, "선호 팀이 정상적으로 저장되었습니다."
        );
        given(favoriteTeamService.setFavoriteTeam(eq(1L), any())).willReturn(resp);

        // when // then
        mockMvc.perform(post("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("선호 팀이 정상적으로 저장되었습니다."));
    }

    @DisplayName("존재하지 않는 팀 ID로는 등록에 실패한다.")
    @Test
    void saveOrUpdateOne_invalidTeamId() throws Exception {
        // given
        FavoriteTeamDto.FavoriteTeamRequest req = new FavoriteTeamDto.FavoriteTeamRequest(999L, 1);
        FavoriteTeamDto.FavoriteTeamResponse resp = new FavoriteTeamDto.FavoriteTeamResponse(
                false, "존재하지 않는 팀 ID입니다."
        );
        given(favoriteTeamService.setFavoriteTeam(eq(1L), any())).willReturn(resp);

        // when // then
        mockMvc.perform(post("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 팀 ID입니다."));
    }

    @DisplayName("여러 선호팀 순서를 정상적으로 일괄 업데이트할 수 있다.")
    @Test
    void updateMany_success() throws Exception {
        // given
        var reqs = List.of(
                new FavoriteTeamDto.FavoriteTeamRequest(7L, 2),
                new FavoriteTeamDto.FavoriteTeamRequest(8L, 3)
        );
        FavoriteTeamDto.FavoriteTeamResponse resp = new FavoriteTeamDto.FavoriteTeamResponse(
                true, "선호 팀이 정상적으로 업데이트되었습니다."
        );
        given(favoriteTeamService.updateFavoriteTeams(eq(1L), anyList())).willReturn(resp);

        // when // then
        mockMvc.perform(put("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqs)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("선호 팀이 정상적으로 업데이트되었습니다."));
    }

    @DisplayName("요청 목록이 비어 있으면 업데이트에 실패한다.")
    @Test
    void updateMany_emptyList() throws Exception {
        // given
        var reqs = List.<FavoriteTeamDto.FavoriteTeamRequest>of();
        FavoriteTeamDto.FavoriteTeamResponse resp = new FavoriteTeamDto.FavoriteTeamResponse(
                false, "최소 한 개의 선호팀은 선택해야 합니다."
        );
        given(favoriteTeamService.updateFavoriteTeams(eq(1L), anyList())).willReturn(resp);

        // when // then
        mockMvc.perform(put("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqs)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("최소 한 개의 선호팀은 선택해야 합니다."));
    }

    @DisplayName("우선순위가 중복된 요청은 실패 메시지를 응답한다.")
    @Test
    void updateMany_duplicateOrder() throws Exception {
        // given
        var reqs = List.of(
                new FavoriteTeamDto.FavoriteTeamRequest(7L, 2),
                new FavoriteTeamDto.FavoriteTeamRequest(8L, 2)
        );
        FavoriteTeamDto.FavoriteTeamResponse resp = new FavoriteTeamDto.FavoriteTeamResponse(
                false, "우선순위가 중복되었습니다: 2"
        );
        given(favoriteTeamService.updateFavoriteTeams(eq(1L), anyList())).willReturn(resp);

        // when // then
        mockMvc.perform(put("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqs)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("우선순위가 중복되었습니다: 2"));
    }
}
