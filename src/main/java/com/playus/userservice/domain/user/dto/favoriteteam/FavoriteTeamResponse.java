package com.playus.userservice.domain.user.dto.favoriteteam;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * created == true  → POST 시  201 Created
 * created == false → PUT 시   200 OK
 */

@Builder
@Jacksonized
public record FavoriteTeamResponse(
        boolean success,
        String message,
        boolean created
) {
    public static FavoriteTeamResponse created(String message) {
        return FavoriteTeamResponse.builder()
                .success(true)
                .message(message)
                .created(true)
                .build();
    }

    public static FavoriteTeamResponse updated(String message) {
        return FavoriteTeamResponse.builder()
                .success(true)
                .message(message)
                .created(false)
                .build();
    }

    public static FavoriteTeamResponse failure(String message) {
        return FavoriteTeamResponse.builder()
                .success(false)
                .message(message)
                .created(false)
                .build();
    }
}