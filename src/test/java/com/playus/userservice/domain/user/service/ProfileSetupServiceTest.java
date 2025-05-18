package com.playus.userservice.domain.user.service;

import com.playus.userservice.IntegrationTestSupport;
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

    @DisplayName("프로필을(닉네임 + 선호팀) 정상적으로 설정한다")
    @Test
    void setupProfile_success() {
        // given
        User user = userRepository.save(
                User.create(
                        "test1",
                        "010-1111-2222",
                        LocalDate.of(1995, 5, 5),
                        Gender.MALE,
                        Role.USER,
                        AuthProvider.KAKAO,
                        "http://example.com/old.jpg"
                )
        );
        Long userId = user.getId();
        Long teamId = Team.NC_DINOS.id();
        String newNickname = "newNick";

        // when
        UserRegisterResponse resp = profileSetupService.setupProfile(userId, teamId, newNickname);

        // then
        assertThat(resp.success()).isTrue();
        assertThat(resp.message()).isEqualTo("프로필이 정상적으로 설정되었습니다.");

        // 닉네임 반영
        User updated = userRepository.findById(userId).get();
        assertThat(updated.getNickname()).isEqualTo(newNickname);

        // 선호팀 저장
        Optional<FavoriteTeam> ftOpt = favoriteTeamRepository.findOneByUser(updated);
        assertThat(ftOpt).isPresent();
        FavoriteTeam ft = ftOpt.get();
        assertThat(ft.getTeamId()).isEqualTo(teamId);
        assertThat(ft.getDisplayOrder()).isEqualTo(1);
    }

    @DisplayName("이미 사용 중인 닉네임으로 프로필을 설정하면 CONFLICT 예외가 발생한다")
    @Test
    void setupProfile_nicknameConflict() {
        // given
        userRepository.save(
                User.create(
                        "test1",
                        "010-0000-0001",
                        LocalDate.of(1990, 1, 1),
                        Gender.FEMALE,
                        Role.USER,
                        AuthProvider.KAKAO,
                        "http://example.com/a.jpg"
                )
        );
        User other = userRepository.save(
                User.create(
                        "test2",
                        "010-0000-0002",
                        LocalDate.of(1991, 2, 2),
                        Gender.MALE,
                        Role.USER,
                        AuthProvider.KAKAO,
                        "http://example.com/b.jpg"
                )
        );

        Long teamId = Team.LG_TWINS.id();

        // when & then
        assertThatThrownBy(() ->
                profileSetupService.setupProfile(other.getId(), teamId, "test1")
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> {
                    ResponseStatusException ex = (ResponseStatusException) e;
                    assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
                    assertThat(ex.getReason()).isEqualTo("이미 사용 중인 닉네임입니다.");
                });
    }
}
