package com.playus.userservice.domain.user.controller;

import com.playus.userservice.domain.user.dto.nickname.NicknameRequest;
import com.playus.userservice.domain.user.dto.nickname.NicknameResponse;
import com.playus.userservice.domain.user.specification.UserControllerSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.service.UserService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController implements UserControllerSpecification {

    private final UserService userService;


    @PutMapping("/nickname")
    public ResponseEntity<NicknameResponse> updateNickname(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody NicknameRequest request) {

        Long userId = Long.parseLong(principal.getName());
        NicknameResponse resp = userService.updateNickname(userId, request);
        return ResponseEntity.ok(resp);
    }

}
