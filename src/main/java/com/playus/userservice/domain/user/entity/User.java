package com.playus.userservice.domain.user.entity;

import com.playus.userservice.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.AuthProvider;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE users SET activated = false WHERE id = ?")
@Table(name = "users")

public class User extends BaseTimeEntity {

    public static final float DEFAULT_SCORE = 0.3f;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String nickname;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

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

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Builder
    private User(String nickname,String phoneNumber, LocalDate birth, Gender gender, Role role, AuthProvider authProvider, boolean activated, LocalDateTime blockOff, LocalDateTime withdrawnAt, String thumbnailURL, Float userScore) {
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
        this.gender = gender;
        this.role = role;
        this.authProvider = authProvider;
        this.activated = activated;
        this.blockOff = blockOff;
        this.withdrawnAt = withdrawnAt;
        this.thumbnailURL = thumbnailURL;
        this.userScore = userScore;
    }

    public static User create(String nickname,String phoneNumber, LocalDate birth, Gender gender, Role role, AuthProvider authProvider, String thumbnailURL) {
        return User.builder()
                .nickname(nickname)
                .phoneNumber(phoneNumber)
                .birth(birth)
                .gender(gender)
                .role(role)
                .authProvider(authProvider)
                .activated(true)
                .blockOff(null)
                .withdrawnAt(null)
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
        this.withdrawnAt = LocalDateTime.now();
    }

    public boolean enableReactivate(int days) {
        return withdrawnAt != null &&
                withdrawnAt.isAfter(LocalDateTime.now().minusDays(days));
    }

    public void reactivate() {
        this.activated   = true;
        this.withdrawnAt = null;
    }
}
