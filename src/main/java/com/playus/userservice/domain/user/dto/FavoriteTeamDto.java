package com.playus.userservice.domain.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class FavoriteTeamDto {

    private FavoriteTeamDto() {}

    public static class FavoriteTeamRequest {

        @NotNull
        private final Long teamId;

        @Min(1) @Max(10)
        private final Integer displayOrder;

        public FavoriteTeamRequest(Long teamId, Integer displayOrder) {
            this.teamId = teamId;
            this.displayOrder = displayOrder;
        }
        public Long getTeamId()      { return teamId; }
        public Integer getDisplayOrder() { return displayOrder; }

    }
    public static class FavoriteTeamResponse {
        private final boolean success;
        private final String  message;

        public FavoriteTeamResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        public boolean isSuccess() { return success; }
        public String  getMessage() { return message; }
    }
}
