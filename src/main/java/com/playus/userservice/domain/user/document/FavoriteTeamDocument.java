package com.playus.userservice.domain.user.document;

import com.playus.userservice.domain.common.BaseTimeEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "favorite_team")
public class FavoriteTeamDocument extends BaseTimeEntity {

    @Id
    private Long id;

    @NotNull
    @Field("user_id")
    private Long userId;

    @NotNull
    @Field("team_id")
    private Long teamId;

    @NotNull
    @Field("display_order")
    private Integer displayOrder;

    @Builder
    private FavoriteTeamDocument(Long id, Long userId, Long teamId, Integer displayOrder) {
        this.id = id;
        this.userId = userId;
        this.teamId = teamId;
        this.displayOrder = displayOrder;
    }

    public static FavoriteTeamDocument createFavoriteTeamDocument(Long id, Long userId, Long teamId, Integer displayOrder
    ) {
        return FavoriteTeamDocument.builder()
                .id(id)
                .userId(userId)
                .teamId(teamId)
                .displayOrder(displayOrder)
                .build();
    }
}
