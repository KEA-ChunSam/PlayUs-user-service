package com.playus.userservice.domain.user.controller;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.UserInfoResponse;
import com.playus.userservice.domain.user.dto.profile.UserProfileResponse;
import com.playus.userservice.domain.user.service.UserProfileReadService;
import com.playus.userservice.domain.user.specification.UserProfileControllerSpecification;
import com.playus.userservice.domain.user.dto.profile.UserPublicProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProfileController implements UserProfileControllerSpecification {

    private final UserProfileReadService userProfileReadService;

    // 내 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal CustomOAuth2User principal) {

        Long userId = Long.parseLong(principal.getName());
        UserProfileResponse response = userProfileReadService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    // 다른 사람 프로필 조회
    @GetMapping("/profile/{user-id}")
    public ResponseEntity<UserPublicProfileResponse> getPublicProfile(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @PathVariable("user-id") Long targetUserId) {

        Long userId = Long.parseLong(principal.getName());
        UserPublicProfileResponse response = userProfileReadService.getPublicProfile(userId,targetUserId);
        return ResponseEntity.ok(response);
    }

    // 다른 사람 프로필 조회 (nickname, profileImageUrl)
    @GetMapping("/simple-profile/{user-id}")
    public ResponseEntity<UserInfoResponse> getUserInfo (
            @PathVariable("user-id") Long targetUserId) {

        UserInfoResponse response = userProfileReadService.getPublicProfileOnlyNicknameAndImageUrl(targetUserId);
        return ResponseEntity.ok(response);
    }
}
