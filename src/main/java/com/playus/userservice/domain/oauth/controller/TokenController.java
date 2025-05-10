package com.playus.userservice.domain.oauth.controller;

import com.playus.userservice.domain.oauth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest req, HttpServletResponse res) {
        authService.verifyAndRenewTokens(req, res);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
        authService.logout(req, res);
        return ResponseEntity.noContent().build();
    }
}
