package com.playus.userservice.domain.user.entity;


import com.playus.userservice.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE user_tag SET activated = false WHERE id = ?")
@Where(clause = "activated = true")
@Table(name = "user_tag", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "tag_id"}, name = "uk_user_tag")
})
public class UserTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Builder
    private UserTag(User user, Tag tag) {
        this.user = user;
        this.tag = tag;
    }

    public static UserTag create(User user, Tag tag){
        return UserTag.builder()
                .user(user)
                .tag(tag)
                .build();
    }

}
