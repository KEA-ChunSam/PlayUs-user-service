package com.playus.userservice.domain.user.dto;

public record UserReviewRequest(
        Long userId,
        Long tagId,
        boolean positive    // true면, +0.01, false면 –0.01
) { }