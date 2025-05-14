package com.playus.userservice.domain.user.controller;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.FavoriteTeamDto;
import com.playus.userservice.domain.user.service.FavoriteTeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/favorite-teams")
@RequiredArgsConstructor
public class FavoriteTeamController {

    private final FavoriteTeamService favoriteTeamService;

    @PostMapping
    public ResponseEntity<FavoriteTeamDto.FavoriteTeamResponse> saveOrUpdateOne(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody FavoriteTeamDto.FavoriteTeamRequest request) {

        Long userId = Long.parseLong(principal.getName());
        FavoriteTeamDto.FavoriteTeamResponse resp = favoriteTeamService
                .setFavoriteTeam(userId, request);
        return ResponseEntity.ok(resp);
    }

    @PutMapping
    public ResponseEntity<FavoriteTeamDto.FavoriteTeamResponse> updateMany(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody List<FavoriteTeamDto.FavoriteTeamRequest> requests) {

        Long userId = Long.parseLong(principal.getName());
        FavoriteTeamDto.FavoriteTeamResponse resp = favoriteTeamService
                .updateFavoriteTeams(userId, requests);
        return ResponseEntity.ok(resp);
    }

    /**
     * 요청 Body
     *
     * [
     *   { "teamId": 2, "displayOrder": 1 },
     *   { "teamId": 5, "displayOrder": 2 },
     *   { "teamId": 9, "displayOrder": 3 }
     * ]
     *
     * 반환
     *
     * { "success": true, "message": "선호팀 목록이 정상적으로 저장되었습니다." }
     * { "success": false, "message": "최소 한 개의 선호팀은 선택해야 합니다." }
     */
}
