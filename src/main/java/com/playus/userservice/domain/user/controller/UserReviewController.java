package com.playus.userservice.domain.user.controller;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.UserReviewRequest;
import com.playus.userservice.domain.user.service.UserReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/reviews")
@RequiredArgsConstructor
public class UserReviewController {

    private final UserReviewService userReviewService;

    @PostMapping
    public ResponseEntity<List<UserReviewRequest>> createUserReviews(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestBody List<UserReviewRequest> requests) {

        Long userId = Long.parseLong(principal.getName());

        List<UserReviewRequest> resp = userReviewService.processReviews(userId, requests);
        return ResponseEntity.ok(resp);
    }
}
