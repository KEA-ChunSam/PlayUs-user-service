package com.playus.userservice.domain.user.controller;


import com.playus.userservice.ControllerTestSupport;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.favoriteteam.FavoriteTeamRequest;
import com.playus.userservice.domain.user.dto.favoriteteam.FavoriteTeamResponse;
import com.playus.userservice.domain.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FavoriteTeamControllerTest extends ControllerTestSupport {

    private UsernamePasswordAuthenticationToken token;

    @BeforeEach
    void setUp() {
        Long userId = 1L;
        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        when(principal.getName()).thenReturn(userId.toString());
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(Role.USER.name()));
        doReturn(authorities).when(principal).getAuthorities();
        token = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    @DisplayName("단일 선호팀을 정상적으로 등록(create)할 수 있다")
    @Test
    void saveOrUpdateOne_created() throws Exception {
        FavoriteTeamRequest req = new FavoriteTeamRequest(7L, 1);
        FavoriteTeamResponse resp = FavoriteTeamResponse.created("선호팀이 정상적으로 등록되었습니다.");
        given(favoriteTeamService.setFavoriteTeam(eq(1L), any())).willReturn(resp);

        mockMvc.perform(post("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("선호팀이 정상적으로 등록되었습니다."))
                .andExpect(jsonPath("$.created").value(true));
    }

    @DisplayName("teamId 필드는 필수이다")
    @ParameterizedTest
    @NullSource
    void saveOrUpdateOne_nullTeamId(Long nullTeamId) throws Exception {
        FavoriteTeamRequest req = new FavoriteTeamRequest(null, 1);
        assertBadRequestOfFavoriteTeamCreateRequest(req, "teamId 필드는 필수입니다.");
    }

    @DisplayName("displayOrder 필드는 필수이다")
    @Test
    void saveOrUpdateOne_nullDisplayOrder() throws Exception {
        String body = "{\"teamId\":7}";
        mockMvc.perform(post("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("displayOrder 필드는 필수입니다."));
    }

    @DisplayName("displayOrder는 1 이상이어야 한다")
    @ParameterizedTest(name = "displayOrder = {0}")
    @ValueSource(ints = {0, -1})
    void saveOrUpdateOne_displayOrderTooLow(int invalidOrder) throws Exception {
        FavoriteTeamRequest req = new FavoriteTeamRequest(7L, invalidOrder);
        assertBadRequestOfFavoriteTeamCreateRequest(req, "displayOrder는 1 이상이어야 합니다.");
    }

    @DisplayName("displayOrder는 10 이하이어야 한다")
    @ParameterizedTest(name = "displayOrder = {0}")
    @ValueSource(ints = {11, 100})
    void saveOrUpdateOne_displayOrderTooHigh(int invalidOrder) throws Exception {
        FavoriteTeamRequest req = new FavoriteTeamRequest(7L, invalidOrder);
        assertBadRequestOfFavoriteTeamCreateRequest(req, "displayOrder는 10 이하이어야 합니다.");
    }

    @DisplayName("여러 선호팀 순서를 정상적으로 일괄 업데이트할 수 있다.")
    @Test
    void updateMany_success() throws Exception {
        List<FavoriteTeamRequest> reqs = List.of(
                new FavoriteTeamRequest(7L, 2),
                new FavoriteTeamRequest(8L, 3)
        );
        FavoriteTeamResponse resp = FavoriteTeamResponse.updated("선호 팀이 정상적으로 업데이트되었습니다.");
        given(favoriteTeamService.updateFavoriteTeams(eq(1L), anyList())).willReturn(resp);

        mockMvc.perform(put("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqs)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("선호 팀이 정상적으로 업데이트되었습니다."))
                .andExpect(jsonPath("$.created").value(false));
    }

    @DisplayName("요청 목록이 비어 있으면 업데이트에 실패한다.")
    @Test
    void updateMany_emptyList() throws Exception {
        List<FavoriteTeamRequest> reqs = List.of();
        FavoriteTeamResponse resp = FavoriteTeamResponse.failure("최소 한 개의 선호팀은 선택해야 합니다.");
        given(favoriteTeamService.updateFavoriteTeams(eq(1L), anyList())).willReturn(resp);

        mockMvc.perform(put("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqs)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("최소 한 개의 선호팀은 선택해야 합니다."));
    }

    @DisplayName("우선순위가 중복된 요청은 실패 메시지를 응답한다.")
    @Test
    void updateMany_duplicateOrder() throws Exception {
        List<FavoriteTeamRequest> reqs = List.of(
                new FavoriteTeamRequest(7L, 2),
                new FavoriteTeamRequest(8L, 2)
        );
        FavoriteTeamResponse resp = FavoriteTeamResponse.failure("우선순위가 중복되었습니다: 2");
        given(favoriteTeamService.updateFavoriteTeams(eq(1L), anyList())).willReturn(resp);

        mockMvc.perform(put("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqs)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("우선순위가 중복되었습니다: 2"));
    }

    private void assertBadRequestOfFavoriteTeamCreateRequest(FavoriteTeamRequest request, String expectedMessage) throws Exception {
        mockMvc.perform(post("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @DisplayName("teamId가 숫자가 아닌 문자열일 때 Bad Request 반환")
    @Test
    void saveOrUpdateOne_typeMismatch() throws Exception {
        String body = "{\"teamId\":\"notANumber\",\"displayOrder\":1}";
        mockMvc.perform(post("/user/favorite-teams")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
