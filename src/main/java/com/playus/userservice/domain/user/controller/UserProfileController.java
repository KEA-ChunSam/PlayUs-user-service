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

/**
 {
 "id":18,
 "nickname":"default_nickname",
 "phoneNumber":"+821079070479",
 "birth":"2000-04-26",
 "gender":"MALE",
 "role":"USER",
 "authProvider":"NAVER",
 "activated":true,
 "thumbnailURL":"default.png",
 "userScore":0.3,
 "blockOff":null,
 "favoriteTeams":[
 {"teamId":8,"displayOrder":1},
 {"teamId":1,"displayOrder":2}
 ]
 }
 */
