package com.playus.userservice.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProfileSetupDto {

    private ProfileSetupDto() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserRegisterRequest {

        @NotNull(message = "favoriteTeam 필드는 필수입니다.")
        private final Long teamId;

        @NotBlank(message = "nickname 필드는 필수입니다.")
        private final String nickname;

        @JsonCreator
        public UserRegisterRequest(
                @JsonProperty("teamId") Long teamId,
                @JsonProperty("nickname") String nickname
        ) {
            this.teamId = teamId;
            this.nickname = nickname;
        }

        public Long getTeamId() {
            return teamId;
        }

        public String getNickname() {
            return nickname;
        }
    }

    public static class UserRegisterResponse {
        private final boolean success;
        private final String message;

        @JsonCreator
        public UserRegisterResponse(
                @JsonProperty("success") boolean success,
                @JsonProperty("message") String message
        ) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
