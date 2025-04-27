package com.playus.userservice.domain.user.document;

import com.playus.userservice.domain.common.BaseTimeEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "user_tag")
public class UserTagDocument extends BaseTimeEntity {
    @Id
    private Long id;

    @NotNull
    @Field("user_id")
    private Long userId;

    @NotNull
    @Field("tag_id")
    private Long tagId;

    @Builder
    private UserTagDocument(Long id, Long userId, Long tagId) {
        this.id = id;
        this.userId = userId;
        this.tagId = tagId;
    }

    public static UserTagDocument createUserTagDocument(Long id, Long userId, Long tagId) {
        return UserTagDocument.builder()
                .id(id)
                .userId(userId)
                .tagId(tagId)
                .build();
    }
}



