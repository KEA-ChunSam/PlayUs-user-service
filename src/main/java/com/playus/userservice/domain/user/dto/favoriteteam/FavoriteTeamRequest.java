package com.playus.userservice.domain.user.dto.favoriteteam;

import com.playus.userservice.domain.user.entity.FavoriteTeam;
import com.playus.userservice.domain.user.entity.User;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record FavoriteTeamRequest(
        @NotNull(message = "teamId 필드는 필수입니다.")
        Long teamId,

        @NotNull(message = "displayOrder 필드는 필수입니다.")
        @Min(value = 1, message = "displayOrder는 1 이상이어야 합니다.")
        @Max(value = 10, message = "displayOrder는 10 이하이어야 합니다.")
        Integer displayOrder
) {

    public FavoriteTeam toEntity(User user) {
        return FavoriteTeam.create(user, teamId, displayOrder);
    }
}