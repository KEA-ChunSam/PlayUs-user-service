package com.playus.userservice.domain.oauth.controller;


import com.playus.userservice.domain.oauth.service.TokenValidationService;
import com.playus.userservice.global.request.TokenValidationRequest;
import com.playus.userservice.global.response.TokenValidationResponse;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api/token")
@RequiredArgsConstructor
public class TokenValidationController {

    private final TokenValidationService tokenValidationService;

    /** 내부 MSA ->  토큰 블랙리스트 여부 조회 */

    @PostMapping("/blacklist-check")
    public ResponseEntity<TokenValidationResponse> checkBlacklist(
            @Valid @RequestBody TokenValidationRequest req) {

        try {
            boolean blacklisted = tokenValidationService.isBlacklisted(req.token());
            return ResponseEntity.ok(new TokenValidationResponse(blacklisted));
        } catch (JwtException | IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new TokenValidationResponse(true));
        }
    }
}
