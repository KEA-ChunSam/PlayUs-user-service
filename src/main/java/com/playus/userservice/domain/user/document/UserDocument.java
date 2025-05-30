package com.playus.userservice.domain.user.document;

import com.playus.userservice.domain.common.BaseTimeEntity;
import com.playus.userservice.domain.user.enums.AuthProvider;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "users")
public class UserDocument extends BaseTimeEntity {
    @Id
    private Long id;

    @NotNull
    @Size(min = 1, max = 225)
    private String nickname;

    @NotNull
    @Field("phone_number")
    @Size(min = 1, max = 20)
    private String phoneNumber;

    @NotNull
    private LocalDate birth;

    @NotNull
    private Gender gender;

    @NotNull
    private Role role;

    @NotNull
    private AuthProvider authProvider;

    @Field("is_activated")
    private boolean activated;

    @Field("thumbnail_url")
    private String thumbnailURL;

    @Field("user_score")
    private Float userScore;

    @Field("block_off")
    private LocalDateTime blockOff;

    @Field("withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Builder
    private UserDocument(Long id, String nickname, String phoneNumber, LocalDate birth, Gender gender, Role role, AuthProvider authProvider, boolean activated, String thumbnailURL, Float userScore, LocalDateTime blockOff, LocalDateTime withdrawnAt) {
        this.id = id;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
        this.gender = gender;
        this.role = role;
        this.authProvider = authProvider;
        this.activated = activated;
        this.thumbnailURL = thumbnailURL;
        this.userScore = userScore;
        this.blockOff = blockOff;
        this.withdrawnAt = withdrawnAt;
    }

    public static UserDocument createUserDocument(Long id, String nickname, String phoneNumber, LocalDate birth, Gender gender, Role role, AuthProvider authProvider, boolean activated, String thumbnailURL, Float userScore, LocalDateTime blockOff, LocalDateTime withdrawnAt) {
        return UserDocument.builder()
                .id(id)
                .nickname(nickname)
                .phoneNumber(phoneNumber)
                .birth(birth)
                .gender(gender)
                .role(role)
                .authProvider(authProvider)
                .activated(activated)
                .thumbnailURL(thumbnailURL)
                .userScore(userScore)
                .blockOff(blockOff)
                .withdrawnAt(withdrawnAt)
                .build();
    }

}
