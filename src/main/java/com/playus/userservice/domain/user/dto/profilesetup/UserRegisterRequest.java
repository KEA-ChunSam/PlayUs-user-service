package com.playus.userservice.domain.user.dto.profilesetup;

import jakarta.validation.constraints.*;

public record UserRegisterRequest(
        @NotNull(message = "favoriteTeam 필드는 필수입니다.")
        Long teamId,

        @NotBlank(message = "nickname 필드는 필수입니다.")
        String nickname
) {}