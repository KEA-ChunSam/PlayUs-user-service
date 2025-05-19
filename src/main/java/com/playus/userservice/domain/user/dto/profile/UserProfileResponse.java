package com.playus.userservice.domain.user.dto.profile;

import com.playus.userservice.domain.user.enums.*;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UserProfileResponse(
        Long id,
        String nickname,
        String phoneNumber,
        LocalDate birth,
        Gender gender,
        Role role,
        AuthProvider authProvider,
        boolean activated,
        String thumbnailURL,
        Float userScore,
        LocalDateTime blockOff,
        List<FavoriteTeamDto> favoriteTeams
) {
}
