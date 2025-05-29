package com.playus.userservice.domain.user.controller;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.UserTagSummaryResponse;
import com.playus.userservice.domain.user.service.UserTagReadService;
import com.playus.userservice.domain.user.specification.UserTagReadControllerSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/{user-id}/tags")
@RequiredArgsConstructor
public class UserTagReadController implements UserTagReadControllerSpecification  {

    private final UserTagReadService userTagReadService;

    @GetMapping("/summary")
    public ResponseEntity<UserTagSummaryResponse> getUserTagSummary(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @PathVariable("user-id") Long userId) {

        Long authUserId = Long.parseLong(principal.getName());
        UserTagSummaryResponse resp = userTagReadService.getUserTagSummary(authUserId,userId);
        return ResponseEntity.ok(resp);
    }
}
