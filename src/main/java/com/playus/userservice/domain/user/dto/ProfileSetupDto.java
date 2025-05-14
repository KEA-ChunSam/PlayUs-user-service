package com.playus.userservice.domain.user.dto;

import jakarta.validation.constraints.*;

public class ProfileSetupDto {

    private ProfileSetupDto() {}

    public static class UserRegisterRequest {

        @NotNull(message = "favoriteTeam 필드는 필수입니다.")
        private final Long teamId;

        @NotBlank(message = "nickname 필드는 필수입니다.")
        private final String nickname;

        public UserRegisterRequest(Long teamId, String nickname) {
            this.teamId = teamId;
            this.nickname = nickname;
        }

        public Long getTeamId()       { return teamId; }
        public String getNickname()     { return nickname; }

    }

    public static class UserRegisterResponse {
        private final boolean success;
        private final String message;

        public UserRegisterResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String  getMessage() { return message; }
    }
}