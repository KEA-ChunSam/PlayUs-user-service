package com.playus.userservice.domain.user.controller;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.ProfileSetupDto.UserRegisterRequest;
import com.playus.userservice.domain.user.dto.ProfileSetupDto.UserRegisterResponse;
import com.playus.userservice.domain.user.service.ProfileSetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.playus.userservice.domain.user.specification.ProfileSetupControllerSpecification;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ProfileSetupController implements ProfileSetupControllerSpecification {

    private final ProfileSetupService setupService;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> setupProfile(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody UserRegisterRequest request) {

        Long userId = Long.parseLong(principal.getName());

        UserRegisterResponse resp = setupService.setupProfile(
                userId,
                request.getTeamId(),
                request.getNickname()
        );
        return ResponseEntity.ok(resp);
    }
}
