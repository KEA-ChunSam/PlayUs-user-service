package com.playus.userservice.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(nullable = false, name = "tag_name", length = 255)
    private String tagName;

    @Builder
    private Tag(String tagName){
        this.tagName = tagName;
    }

    public static Tag create(String tagName){
        return Tag.builder()
                .tagName(tagName)
                .build();
    }
}
