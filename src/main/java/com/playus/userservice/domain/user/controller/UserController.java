package com.playus.userservice.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.NicknameDto;
import com.playus.userservice.domain.user.service.UserService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PutMapping("/nickname")
    public ResponseEntity<NicknameDto.NicknameResponse> updateNickname(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody NicknameDto.NicknameRequest request) {

        Long userId = Long.parseLong(principal.getName());
        NicknameDto.NicknameResponse resp = userService.updateNickname(userId, request);
        return ResponseEntity.ok(resp);
    }

}
