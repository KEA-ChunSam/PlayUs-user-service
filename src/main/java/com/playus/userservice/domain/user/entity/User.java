package com.playus.userservice.domain.user.entity;

import com.playus.userservice.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.AuthProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {

    public static final float DEFAULT_SCORE = 0.3f;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nickname;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Column(nullable = false)
    private boolean activated;

    @Column(name = "thumbnail_url", length = 255, nullable = false)
    private String thumbnailURL;

    @Column(name = "user_score", nullable = false)
    private Float userScore;

    private LocalDateTime blockOff;

    @Builder
    private User(String nickname,String email, LocalDate birth, Gender gender, Role role, AuthProvider authProvider, boolean activated, LocalDateTime blockOff, String thumbnailURL, Float userScore) {
        this.nickname = nickname;
        this.email = email;
        this.birth = birth;
        this.gender = gender;
        this.role = role;
        this.authProvider = authProvider;
        this.activated = activated;
        this.blockOff = blockOff;
        this.thumbnailURL = thumbnailURL;
        this.userScore = userScore;
    }

    public static User create(String nickname,String email, LocalDate birth, Gender gender, Role role, AuthProvider authProvider, String thumbnailURL) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .birth(birth)
                .gender(gender)
                .role(role)
                .authProvider(authProvider)
                .activated(true)
                .blockOff(null)
                .thumbnailURL(thumbnailURL)
                .userScore(DEFAULT_SCORE)
                .build();
    }

    public void updateBlockOff(LocalDateTime blockOff) {
        this.blockOff = blockOff;
    }

    public void updateImage(String thumbnailURL) {this.thumbnailURL = thumbnailURL; }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateUserScore(Float userScore) {
        this.userScore = userScore;
    }

    public void withdrawAccount() {
        this.activated = false;
    }

    public void updateUserInfo(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }

}
