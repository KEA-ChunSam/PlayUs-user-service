package com.playus.userservice.domain.user.controller;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.NicknameDto;
import com.playus.userservice.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    /**
     * PUT /user/nickname
     * Body: { "nickname": "chunsam" }
     */

    private final UserService userService;

    @PutMapping("/nickname")
    public ResponseEntity<NicknameDto.NicknameResponse> updateNickname(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody NicknameDto.NicknameRequest request) {

        // principal.getName()에 userId(String)이 담겨 있으므로 파싱
        Long userId = Long.parseLong(principal.getName());
        NicknameDto.NicknameResponse resp = userService.updateNickname(userId, request);
        return ResponseEntity.ok(resp);
    }
}
