package com.playus.userservice.domain.user.controller;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.feign.client.TwpFeignClient;
import com.playus.userservice.domain.user.feign.response.PartyApplicationInfoFeignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/api")
@RequiredArgsConstructor
public class PartyApplicationController {

    private final TwpFeignClient twpFeignClient;

    // 내가 신청한 직관팟 목록 조회
    @GetMapping("/applications")
    public ResponseEntity<List<PartyApplicationInfoFeignResponse>> getMyApplications(
            @AuthenticationPrincipal CustomOAuth2User principal) {

        Long userId = Long.parseLong(principal.getName());

        List<PartyApplicationInfoFeignResponse> applications = twpFeignClient.getMyApplications(userId);
        return ResponseEntity.ok(applications);
    }
}
