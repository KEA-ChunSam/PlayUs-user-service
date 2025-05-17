package com.playus.userservice.domain.user.service;

import com.playus.userservice.IntegrationTestSupport;
import com.playus.userservice.domain.user.dto.FavoriteTeamDto.FavoriteTeamRequest;
import com.playus.userservice.domain.user.dto.FavoriteTeamDto.FavoriteTeamResponse;
import com.playus.userservice.domain.user.entity.FavoriteTeam;
import com.playus.userservice.domain.user.enums.Team;
import com.playus.userservice.domain.user.repository.write.FavoriteTeamRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class FavoriteTeamServiceTest extends IntegrationTestSupport {

    @Autowired
    private FavoriteTeamService favoriteTeamService;

    @Autowired
    private FavoriteTeamRepository favoriteTeamRepository;

    private final Long USER_ID = 1L;

    @AfterEach
    void tearDown() {
        favoriteTeamRepository.deleteAllInBatch();
    }

    // setFavoriteTeam (1개 건 등록이나 수정)

    @DisplayName("선호팀을 처음 등록하면 success=true 를 반환한다")
    @Test
    void setFavoriteTeam_insert_success() {
        // given
        Long teamId = Team.values()[0].id();   // 유효한 팀 ID
        FavoriteTeamRequest req = new FavoriteTeamRequest(teamId, 1);

        // when
        FavoriteTeamResponse resp = favoriteTeamService.setFavoriteTeam(USER_ID, req);

        // then
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getMessage()).contains("등록");

        FavoriteTeam saved = favoriteTeamRepository.findByUserId(USER_ID)
                .orElseThrow();
        assertThat(saved.getTeamId()).isEqualTo(teamId);
        assertThat(saved.getDisplayOrder()).isEqualTo(1);
    }

    @DisplayName("이미 등록된 선호팀이 있으면 정보가 업데이트된다")
    @Test
    void setFavoriteTeam_update_success() {
        // given
        Long firstTeamId  = Team.values()[0].id();
        Long secondTeamId = Team.values()[1].id();

        favoriteTeamRepository.save(
                FavoriteTeam.create(USER_ID, firstTeamId, 1)
        );

        // when
        FavoriteTeamResponse resp = favoriteTeamService.setFavoriteTeam(
                USER_ID,
                new FavoriteTeamRequest(secondTeamId, 1)
        );

        // then
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getMessage()).contains("변경");

        FavoriteTeam updated = favoriteTeamRepository.findByUserId(USER_ID)
                .orElseThrow();
        assertThat(updated.getTeamId()).isEqualTo(secondTeamId);
        assertThat(updated.getDisplayOrder()).isEqualTo(1);
        assertThat(favoriteTeamRepository.count()).isOne();
    }

    @DisplayName("존재하지 않는 팀 ID 로 등록하면 실패 응답을 반환한다")
    @Test
    void setFavoriteTeam_invalidTeam_fail() {
        // given
        Long invalidTeamId = 9_999L;
        FavoriteTeamRequest req = new FavoriteTeamRequest(invalidTeamId, 1);

        // when
        FavoriteTeamResponse resp = favoriteTeamService.setFavoriteTeam(USER_ID, req);

        // then
        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).contains("존재하지 않는 팀");
        // 데이터가 저장되지 않는다
        assertThat(favoriteTeamRepository.count()).isZero();
    }

    //updateFavoriteTeams(배치 저장)


    @DisplayName("여러 개의 선호팀 목록을 정상적으로 저장한다")
    @Test
    void updateFavoriteTeams_success() {
        // given
        Long team1 = Team.values()[0].id();
        Long team2 = Team.values()[1].id();

        List<FavoriteTeamRequest> reqs = List.of(
                new FavoriteTeamRequest(team1, 1),
                new FavoriteTeamRequest(team2, 2)
        );

        // when
        FavoriteTeamResponse resp = favoriteTeamService.updateFavoriteTeams(USER_ID, reqs);

        // then
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getMessage()).contains("정상적으로 저장");

        List<FavoriteTeam> saved = favoriteTeamRepository.findAll();
        assertThat(saved).hasSize(2)
                .extracting(FavoriteTeam::getDisplayOrder)
                .containsExactlyInAnyOrder(1, 2);
    }

    @DisplayName("우선순위가 중복되면 실패 응답을 반환한다")
    @Test
    void updateFavoriteTeams_duplicateOrder_fail() {
        // given
        Long team1 = Team.values()[0].id();
        Long team2 = Team.values()[1].id();

        List<FavoriteTeamRequest> reqs = List.of(
                new FavoriteTeamRequest(team1, 1),
                new FavoriteTeamRequest(team2, 1)   // 중복
        );

        // when
        FavoriteTeamResponse resp = favoriteTeamService.updateFavoriteTeams(USER_ID, reqs);

        // then
        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).contains("우선순위가 중복");
        assertThat(favoriteTeamRepository.count()).isZero();
    }

    @DisplayName("목록 중 유효하지 않은 팀 ID 가 포함되면 실패 응답을 반환한다")
    @Test
    void updateFavoriteTeams_invalidTeam_fail() {
        // given
        Long validTeam   = Team.values()[0].id();
        Long invalidTeam = 9_999L;

        List<FavoriteTeamRequest> reqs = List.of(
                new FavoriteTeamRequest(validTeam,   1),
                new FavoriteTeamRequest(invalidTeam, 2)   // 존재하지 않는 팀 포함
        );

        // when
        FavoriteTeamResponse resp = favoriteTeamService.updateFavoriteTeams(USER_ID, reqs);

        // then
        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).contains("존재하지 않는 팀");
        assertThat(favoriteTeamRepository.count()).isZero();
    }

    @DisplayName("빈 목록을 넘기면 실패 응답을 반환한다")
    @Test
    void updateFavoriteTeams_emptyList_fail() {
        // when
        FavoriteTeamResponse resp = favoriteTeamService.updateFavoriteTeams(USER_ID, List.of());

        // then
        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).contains("최소 한 개의 선호팀");
    }
}
