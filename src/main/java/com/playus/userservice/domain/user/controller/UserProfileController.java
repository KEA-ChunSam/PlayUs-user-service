package com.playus.userservice.domain.user.controller;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.profile.UserProfileResponse;
import com.playus.userservice.domain.user.service.UserProfileReadService;
import com.playus.userservice.domain.user.specification.UserProfileControllerSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProfileController implements UserProfileControllerSpecification {

    private final UserProfileReadService userProfileReadService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal CustomOAuth2User principal) {

        Long userId = Long.parseLong(principal.getName());
        UserProfileResponse response = userProfileReadService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

}
