package com.playus.userservice.domain.user.dto.profile;

import com.playus.userservice.domain.user.enums.Gender;
import lombok.Builder;

import java.util.List;

@Builder
public record UserPublicProfileResponse(
        Long id,
        String nickname,
        Gender gender,
        String thumbnailURL,
        Float userScore,
        List<FavoriteTeamDto> favoriteTeams
) {}
