package com.playus.userservice.domain.user.controller;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.favoriteteam.FavoriteTeamRequest;
import com.playus.userservice.domain.user.dto.favoriteteam.FavoriteTeamResponse;
import com.playus.userservice.domain.user.service.FavoriteTeamService;
import com.playus.userservice.domain.user.specification.FavoriteTeamControllerSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/favorite-teams")
@RequiredArgsConstructor
public class FavoriteTeamController implements FavoriteTeamControllerSpecification {

    private final FavoriteTeamService favoriteTeamService;

    @PostMapping
    public ResponseEntity<FavoriteTeamResponse> setFavoriteTeam(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody FavoriteTeamRequest request
    ) {
        Long userId = Long.parseLong(principal.getName());
        FavoriteTeamResponse resp = favoriteTeamService.setFavoriteTeam(userId, request);

        HttpStatus status = resp.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(resp);
    }

    @PutMapping
    public ResponseEntity<FavoriteTeamResponse> updateFavoriteTeams(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody List<FavoriteTeamRequest> requests
    ) {
        Long userId = Long.parseLong(principal.getName());
        FavoriteTeamResponse resp = favoriteTeamService.updateFavoriteTeams(userId, requests);

        return ResponseEntity.ok(resp);
    }
}
