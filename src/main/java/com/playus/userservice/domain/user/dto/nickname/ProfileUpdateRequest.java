package com.playus.userservice.domain.user.dto.nickname;

import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateRequest(
        @NotBlank(message = "nickname 필드는 필수입니다.")
        String nickname,

        @NotBlank(message = "thumbnailURL 필드는 필수입니다.")
        String thumbnailURL
) {}
