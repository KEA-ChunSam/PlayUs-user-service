package com.playus.userservice.domain.user.service;

import com.playus.userservice.IntegrationTestSupport;
import com.playus.userservice.domain.user.dto.favoriteteam.FavoriteTeamRequest;
import com.playus.userservice.domain.user.dto.favoriteteam.FavoriteTeamResponse;
import com.playus.userservice.domain.user.entity.FavoriteTeam;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.enums.AuthProvider;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.enums.Team;
import com.playus.userservice.domain.user.repository.write.FavoriteTeamRepository;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FavoriteTeamServiceTest extends IntegrationTestSupport {

    @Autowired
    private FavoriteTeamService favoriteTeamService;

    @Autowired
    private FavoriteTeamRepository favoriteTeamRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(
                User.create(
                        "test1",
                        "010-1111-2222",
                        LocalDate.of(1995, 5, 5),
                        Gender.MALE,
                        Role.USER,
                        AuthProvider.KAKAO,
                        "http://example.com/profile.jpg"
                )
        );
    }

    @AfterEach
    void tearDown() {
        favoriteTeamRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    // setFavoriteTeam (1개 건 등록이나 수정)

    @DisplayName("선호팀을 처음 등록하면 success=true, created=true 를 반환한다")
    @Test
    void setFavoriteTeam_insert_success() {
        // given
        Long userId = testUser.getId();
        Long teamId = Team.NC_DINOS.id();
        FavoriteTeamRequest req = new FavoriteTeamRequest(teamId, 1);

        // when
        FavoriteTeamResponse resp = favoriteTeamService.setFavoriteTeam(userId, req);

        // then
        assertThat(resp.success()).isTrue();
        assertThat(resp.created()).isTrue();
        assertThat(resp.message()).contains("등록");

        User user = userRepository.findById(userId).get();
        FavoriteTeam saved = favoriteTeamRepository.findOneByUser(user).get();
        assertThat(saved.getTeamId()).isEqualTo(teamId);
        assertThat(saved.getDisplayOrder()).isEqualTo(1);
    }

    @DisplayName("이미 등록된 선호팀이 있으면 success=true, created=false 로 정보가 업데이트된다")
    @Test
    void setFavoriteTeam_update_success() {
        // given
        Long userId       = testUser.getId();
        Long firstTeamId  = Team.values()[0].id();
        Long secondTeamId = Team.values()[1].id();

        favoriteTeamRepository.save(FavoriteTeam.create(testUser, firstTeamId, 1));

        // when
        FavoriteTeamResponse resp = favoriteTeamService.setFavoriteTeam(
                userId,
                new FavoriteTeamRequest(secondTeamId, 1)
        );

        // then
        assertThat(resp.success()).isTrue();
        assertThat(resp.created()).isFalse();
        assertThat(resp.message()).contains("변경");

        FavoriteTeam updated = favoriteTeamRepository.findOneByUser(testUser).get();
        assertThat(updated.getTeamId()).isEqualTo(secondTeamId);
        assertThat(updated.getDisplayOrder()).isEqualTo(1);
        assertThat(favoriteTeamRepository.count()).isOne();
    }

    @DisplayName("존재하지 않는 팀 ID 로 등록하면 success=false, created=false 로 실패 응답을 반환한다")
    @Test
    void setFavoriteTeam_invalidTeam_fail() {
        // given
        Long userId = testUser.getId();
        Long invalidTeamId = 9_999L;

        FavoriteTeamRequest req = new FavoriteTeamRequest(invalidTeamId, 1);

        // when
        FavoriteTeamResponse resp = favoriteTeamService.setFavoriteTeam(userId, req);

        // then
        assertThat(resp.success()).isFalse();
        assertThat(resp.created()).isFalse();
        assertThat(resp.message()).contains("존재하지 않는 팀");
        assertThat(favoriteTeamRepository.count()).isZero();
    }

    // updateFavoriteTeams (배치 저장)

    @DisplayName("여러 개의 선호팀 목록을 정상적으로 저장하면 success=true, created=false 를 반환한다")
    @Test
    void updateFavoriteTeams_success() {
        // given
        Long userId = testUser.getId();
        Long team1  = Team.values()[0].id();
        Long team2  = Team.values()[1].id();

        List<FavoriteTeamRequest> reqs = List.of(
                new FavoriteTeamRequest(team1, 1),
                new FavoriteTeamRequest(team2, 2)
        );

        // when
        FavoriteTeamResponse resp = favoriteTeamService.updateFavoriteTeams(userId, reqs);

        // then
        assertThat(resp.success()).isTrue();
        assertThat(resp.created()).isFalse();
        assertThat(resp.message()).contains("정상적으로 저장");

        User user = userRepository.findById(userId).get();
        List<FavoriteTeam> saved = favoriteTeamRepository.findAllByUser(user);
        assertThat(saved).hasSize(2)
                .extracting(FavoriteTeam::getDisplayOrder)
                .containsExactlyInAnyOrder(1, 2);
    }

    @DisplayName("우선순위가 중복되면 success=false, created=false 로 실패 응답을 반환한다")
    @Test
    void updateFavoriteTeams_duplicateOrder_fail() {
        // given
        Long userId = testUser.getId();
        Long team1  = Team.values()[0].id();
        Long team2  = Team.values()[1].id();

        List<FavoriteTeamRequest> reqs = List.of(
                new FavoriteTeamRequest(team1, 1),
                new FavoriteTeamRequest(team2, 1)   // 중복
        );

        // when
        FavoriteTeamResponse resp = favoriteTeamService.updateFavoriteTeams(userId, reqs);

        // then
        assertThat(resp.success()).isFalse();
        assertThat(resp.created()).isFalse();
        assertThat(resp.message()).contains("우선순위가 중복되었습니다");
        assertThat(favoriteTeamRepository.count()).isZero();
    }

    @DisplayName("목록 중 유효하지 않은 팀 ID 가 포함되면 success=false, created=false 로 실패 응답을 반환한다")
    @Test
    void updateFavoriteTeams_invalidTeam_fail() {
        // given
        Long userId      = testUser.getId();
        Long validTeam   = Team.values()[0].id();
        Long invalidTeam = 9_999L;

        List<FavoriteTeamRequest> reqs = List.of(
                new FavoriteTeamRequest(validTeam,   1),
                new FavoriteTeamRequest(invalidTeam, 2)
        );

        // when
        FavoriteTeamResponse resp = favoriteTeamService.updateFavoriteTeams(userId, reqs);

        // then
        assertThat(resp.success()).isFalse();
        assertThat(resp.created()).isFalse();
        assertThat(resp.message()).contains("존재하지 않는 팀");
        assertThat(favoriteTeamRepository.count()).isZero();
    }

    @DisplayName("빈 목록을 넘기면 success=false, created=false 로 실패 응답을 반환한다")
    @Test
    void updateFavoriteTeams_emptyList_fail() {
        // given
        Long userId = testUser.getId();

        // when
        FavoriteTeamResponse resp = favoriteTeamService.updateFavoriteTeams(userId, List.of());

        // then
        assertThat(resp.success()).isFalse();
        assertThat(resp.created()).isFalse();
        assertThat(resp.message()).contains("최소 한 개의 선호팀");
    }
}
