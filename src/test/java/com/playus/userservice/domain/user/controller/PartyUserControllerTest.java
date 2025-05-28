package com.playus.userservice.domain.user.controller;

import com.playus.userservice.ControllerTestSupport;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.partyuser.PartyUserThumbnailUrlListResponse;
import com.playus.userservice.domain.user.dto.partyuser.PartyWriterInfoFeignResponse;
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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PartyUserControllerTest extends ControllerTestSupport {

    private UsernamePasswordAuthenticationToken token;

    @BeforeEach
    void setUp() {
        Long userId = 1L;
        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        Mockito.when(principal.getName()).thenReturn(userId.toString());

        var authorities = List.of(new SimpleGrantedAuthority(Role.USER.name()));
        token = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    @DisplayName("/thumbnails : 정상적으로 URL 리스트를 반환한다")
    @Test
    void getThumbnails_success() throws Exception {
        List<String> urls = List.of("https://img1", "https://img2");
        given(userProfileReadService.fetchThumbnailUrls(anyList()))
                .willReturn(urls);

        var expectedResp = new PartyUserThumbnailUrlListResponse(urls);

        mockMvc.perform(post("/user/api/thumbnails")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(10L, 11L))))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResp)));
    }

    @DisplayName("/thumbnails : user가 없으면 404 반환")
    @Test
    void getThumbnails_notFound() throws Exception {
        willThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                .given(userProfileReadService).fetchThumbnailUrls(anyList());

        mockMvc.perform(post("/user/api/thumbnails")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(99L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message")
                        .value("사용자를 찾을 수 없습니다."));
    }

    @DisplayName("/writers : 작성자 정보를 정상적으로 반환한다")
    @Test
    void getWriters_success() throws Exception {
        List<PartyWriterInfoFeignResponse> writerInfos = List.of(
                new PartyWriterInfoFeignResponse(1L, "nick1",20, "MALE", "url1"),
                new PartyWriterInfoFeignResponse(2L, "nick2", 30, "FEMALE", "url2")
        );
        given(userProfileReadService.fetchWriterInfos(eq(List.of(1L, 2L))))
                .willReturn(writerInfos);

        mockMvc.perform(post("/user/api/writers")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(writerInfos)));
    }

    @DisplayName("/writers : 잘못된 요청 값이면 400 반환")
    @Test
    void getWriters_badRequest() throws Exception {
        willThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "id 값이 잘못되었습니다."))
                .given(userProfileReadService).fetchWriterInfos(anyList());

        mockMvc.perform(post("/user/api/writers")
                        .with(authentication(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("id 값이 잘못되었습니다."));
    }
}
