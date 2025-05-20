package com.playus.userservice.domain.user.entity;

import com.playus.userservice.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tag", uniqueConstraints = {
        @UniqueConstraint(columnNames = "tag_name", name = "uk_tag_name")
})
@SQLDelete(sql = "UPDATE tag SET activated = false WHERE id = ?")
public class Tag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
