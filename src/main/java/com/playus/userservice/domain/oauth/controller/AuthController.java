package com.playus.userservice.domain.oauth.controller;

import com.playus.userservice.domain.oauth.service.AuthService;
import com.playus.userservice.domain.oauth.specification.AuthControllerSpecification;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerSpecification {

    private final AuthService authService;

    @Override
    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue() {
        // 현재 스레드의 Request/Response 획득
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest  req = attrs.getRequest();
        HttpServletResponse res = attrs.getResponse();

        authService.verifyAndRenewToken(req, res);
        return ResponseEntity.ok().build();
    }

    /** 로그아웃 */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest  req = attrs.getRequest();
        HttpServletResponse res = attrs.getResponse();

        authService.logout(req, res);
        return ResponseEntity.noContent().build();
    }
}
