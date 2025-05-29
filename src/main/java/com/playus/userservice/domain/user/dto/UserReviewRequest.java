package com.playus.userservice.domain.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserReviewRequest(
        @NotNull(message = "userId는 필수입니다.")
        Long userId,
        @NotNull(message = "tagId는 필수입니다.")
        Long tagId,
        boolean positive    // true면, +0.01, false면 –0.01
) {}
