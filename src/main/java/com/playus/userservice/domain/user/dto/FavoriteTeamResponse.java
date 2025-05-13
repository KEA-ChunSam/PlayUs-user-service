package com.playus.userservice.domain.user.dto;

public class FavoriteTeamResponse {
    private final boolean success;
    private final String  message;

    public FavoriteTeamResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public boolean isSuccess() { return success; }
    public String  getMessage() { return message; }
}