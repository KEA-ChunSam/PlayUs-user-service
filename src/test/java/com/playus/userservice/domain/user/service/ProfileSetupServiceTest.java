package com.playus.userservice.domain.user.service;

import com.playus.userservice.IntegrationTestSupport;
import com.playus.userservice.domain.user.dto.presigned.PresignedUrlForSaveImageRequest;
import com.playus.userservice.domain.user.dto.presigned.PresignedUrlForSaveImageResponse;
import com.playus.userservice.domain.user.dto.profilesetup.UserRegisterResponse;
import com.playus.userservice.domain.user.entity.FavoriteTeam;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.enums.AuthProvider;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.enums.Team;
import com.playus.userservice.domain.user.repository.write.FavoriteTeamRepository;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class ProfileSetupServiceTest extends IntegrationTestSupport {

    @Autowired
    private ProfileSetupService profileSetupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoriteTeamRepository favoriteTeamRepository;

    @AfterEach
    void tearDown() {
        favoriteTeamRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("프로필(닉네임,팀,썸네일)을 정상적으로 설정한다")
    @Test
    void setupProfile_success() {
        // given
        User user = userRepository.save(
                User.create("test1", "010-1111-2222",
                        LocalDate.of(1995, 5, 5),
                        Gender.MALE, Role.USER,
                        AuthProvider.KAKAO, "http://old.jpg"));
        Long userId = user.getId();
        Long teamId = Team.NC_DINOS.id();
        String newNick = "newNick";
        String newUrl  = "http://new.jpg";

        // when
        UserRegisterResponse resp =
                profileSetupService.setupProfile(userId, teamId, newNick, newUrl);

        // then
        assertThat(resp.success()).isTrue();
        User updated = userRepository.findById(userId).get();
        assertThat(updated.getNickname()).isEqualTo(newNick);
        assertThat(updated.getThumbnailURL()).isEqualTo(newUrl);

        Optional<FavoriteTeam> ftOpt = favoriteTeamRepository.findOneByUser(updated);
        assertThat(ftOpt).isPresent();
        assertThat(ftOpt.get().getTeamId()).isEqualTo(teamId);
        assertThat(ftOpt.get().getDisplayOrder()).isEqualTo(1);
    }

    @DisplayName("이미 사용 중인 닉네임이면 CONFLICT 예외가 발생한다")
    @Test
    void setupProfile_nicknameConflict() {
        // given
        userRepository.save(
                User.create("dup", "010-0000-0001",
                        LocalDate.of(1990, 1, 1),
                        Gender.FEMALE, Role.USER,
                        AuthProvider.KAKAO, "http://a.jpg"));

        User other = userRepository.save(
                User.create("other", "010-0000-0002",
                        LocalDate.of(1991, 2, 2),
                        Gender.MALE, Role.USER,
                        AuthProvider.KAKAO, "http://b.jpg"));

        // when // then
        assertThatThrownBy(() ->
                profileSetupService.setupProfile(
                        other.getId(), Team.LG_TWINS.id(), "dup", "http://c.jpg"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> {
                    ResponseStatusException ex = (ResponseStatusException) e;
                    assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
                    assertThat(ex.getReason()).isEqualTo("이미 사용 중인 닉네임입니다.");
                });
    }

    @DisplayName("Presigned URL을 정상 발급한다")
    @Test
    void generatePresignedUrl_success() {

        // given
        String expected = "https://pre-signed-url.com";
        given(s3Service.generatePresignedUrl("img.jpg")).willReturn(expected);

        // when
        PresignedUrlForSaveImageResponse resp =
                profileSetupService.generatePresignedUrlForSaveImage(
                        new PresignedUrlForSaveImageRequest("img.jpg"));

        // then
        assertThat(resp.presignedUrl()).isEqualTo(expected);
        then(s3Service).should().generatePresignedUrl("img.jpg");
    }

    @DisplayName("존재하지 않는 사용자면 NOT_FOUND 예외가 발생한다")
    @Test
    void setupProfile_userNotFound() {

        // given : DB 비어 있음 (userId = 999L)
        Long ghostId = 999L;

        // when & then
        assertThatThrownBy(() ->
                profileSetupService.setupProfile(
                        ghostId, Team.LG_TWINS.id(), "nick", "http://img.jpg"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> {
                    ResponseStatusException ex = (ResponseStatusException) e;
                    assertThat(ex.getStatusCode())
                            .isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason())
                            .isEqualTo("사용자를 찾을 수 없습니다.");
                });
    }

}
