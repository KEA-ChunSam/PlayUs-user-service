package com.playus.userservice.domain.user.dto.profile;

import lombok.Builder;

@Builder
public record FavoriteTeamDto(
        Long teamId,
        Integer displayOrder
) {
}
