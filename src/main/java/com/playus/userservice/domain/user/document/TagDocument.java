package com.playus.userservice.domain.user.document;

import com.playus.userservice.domain.common.BaseTimeEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "tag")
public class TagDocument extends BaseTimeEntity {
    @Id
    private Long id;

    @NotNull
    @Size(max = 255)
    @Field("tag_name")
    private String tagName;

    @Builder
    private TagDocument(Long id, String tagName) {
        this.id = id;
        this.tagName = tagName;
    }

    public static TagDocument createTagDocument(Long id, String tagName) {
        return TagDocument.builder()
                .id(id)
                .tagName(tagName)
                .build();
    }
}
