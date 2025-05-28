package com.playus.userservice.global.request;

import jakarta.validation.constraints.NotBlank;

public record TokenValidationRequest(
        @NotBlank(message = "토큰은 필수 입력값입니다")
        String token
) {}
