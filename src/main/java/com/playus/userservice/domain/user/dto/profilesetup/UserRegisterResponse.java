package com.playus.userservice.domain.user.dto.profilesetup;

public record UserRegisterResponse(
        boolean success,
        String  message
) {}