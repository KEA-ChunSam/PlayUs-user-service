package com.playus.userservice.domain.user.dto;

public record UserInfoResponse(
        String nickname,
        String profileImageUrl
) {
}