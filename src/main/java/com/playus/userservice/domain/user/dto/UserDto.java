package com.playus.userservice.domain.user.dto;

import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.enums.AuthProvider;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserDto {

    private Long id;
    private String nickname;
    private LocalDate birth;
    private Gender gender;
    private Role role;
    private AuthProvider authProvider;
    private boolean activated;
    private String thumbnailURL;
    private Float userScore;
    private LocalDateTime blockOff;
    private LocalDateTime createdAt;

    public UserDto(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.birth = user.getBirth();
        this.gender = user.getGender();
        this.role = user.getRole();
        this.authProvider = user.getAuthProvider();
        this.activated = user.isActivated();
        this.thumbnailURL = user.getThumbnailURL();
        this.userScore = user.getUserScore();
        this.blockOff = user.getBlockOff();
        this.createdAt = user.getCreatedAt();
    }

    //JWT필터에서 사용
    public static UserDto fromJwt(Long id, Role role) {
        UserDto dto = new UserDto();
        dto.id = id;
        dto.role = role;
        return dto;
    }
}
