package com.playus.userservice.domain.user.service;

import com.playus.userservice.IntegrationTestSupport;
import com.playus.userservice.domain.user.document.FavoriteTeamDocument;
import com.playus.userservice.domain.user.document.UserDocument;
import com.playus.userservice.domain.user.dto.profile.UserProfileResponse;
import com.playus.userservice.domain.user.dto.profile.UserPublicProfileResponse;
import com.playus.userservice.domain.user.enums.AuthProvider;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.repository.read.FavoriteTeamReadOnlyRepository;
import com.playus.userservice.domain.user.repository.read.UserReadOnlyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class UserProfileReadServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserProfileReadService userProfileReadService;

    @Autowired
    private UserReadOnlyRepository userRepo;

    @Autowired
    private FavoriteTeamReadOnlyRepository teamRepo;

    @AfterEach
    void tearDown() {
        teamRepo.deleteAll();
        userRepo.deleteAll();
    }

    @DisplayName("사용자 + 선호팀 문서를 모두 저장한 뒤 프로필을 정상적으로 조회한다")
    @Test
    void getProfile_success() {
        // given
        Long userId = 18L;

        UserDocument userDoc = UserDocument.createUserDocument(
                userId,
                "default_nickname",
                "+821079070479",
                LocalDate.of(2000, 4, 26),
                Gender.MALE,
                Role.USER,
                AuthProvider.NAVER,
                true,
                "default.png",
                0.3f,
                null,
                null
        );
        userRepo.save(userDoc);

        //
        teamRepo.save(FavoriteTeamDocument.createFavoriteTeamDocument(1L, userId, 8L, 1));
        teamRepo.save(FavoriteTeamDocument.createFavoriteTeamDocument(2L, userId, 1L, 2));

        //when
        UserProfileResponse result = userProfileReadService.getProfile(userId);

        // then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.nickname()).isEqualTo("default_nickname");
        assertThat(result.favoriteTeams())
                .extracting("teamId", "displayOrder")
                .containsExactly(
                        tuple(8L, 1),
                        tuple(1L, 2)
                );
    }

    @DisplayName("사용자 문서가 없으면 404 ResponseStatusException 을 던진다")
    @Test
    void getProfile_notFound() {
        // given
        Long invalidId = 999L;

        // when // then
        assertThatThrownBy(() -> userProfileReadService.getProfile(invalidId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }


    //다른 사용자 프로필
    @DisplayName("요청자와 대상 사용자 문서를 모두 저장한 뒤 공개 프로필을 정상 조회한다")
    @Test
    void getPublicProfile_success() {
        // given
        Long requesterId = 18L;
        Long targetId    = 99L;

        userRepo.save(UserDocument.createUserDocument(
                requesterId,
                "me",
                "+820101",
                LocalDate.now(),
                Gender.MALE,
                Role.USER,
                AuthProvider.KAKAO,
                true,
                "me.png",
                1.0f,
                null,
                null));

        userRepo.save(UserDocument.createUserDocument(
                targetId,
                "other_user",
                "+820102",
                LocalDate.now(),
                Gender.FEMALE,
                Role.USER,
                AuthProvider.NAVER,
                true,
                "other.png",
                2.5f,
                null,
                null));

        teamRepo.save(FavoriteTeamDocument.createFavoriteTeamDocument(1L, targetId, 3L, 1));

        // when
        UserPublicProfileResponse result = userProfileReadService.getPublicProfile(requesterId, targetId);

        // then
        assertThat(result.id()).isEqualTo(targetId);
        assertThat(result.nickname()).isEqualTo("other_user");
        assertThat(result.favoriteTeams())
                .extracting("teamId", "displayOrder")
                .containsExactly(tuple(3L, 1));
    }

    @DisplayName("대상 사용자 문서가 없으면 404 ResponseStatusException")
    @Test
    void getPublicProfile_targetNotFound() {
        // given
        Long requesterId = 18L;
        Long invalidId   = 999L;

        userRepo.save(UserDocument.createUserDocument(
                requesterId,
                "me",
                "+820101",
                LocalDate.now(),
                Gender.MALE,
                Role.USER,
                AuthProvider.KAKAO,
                true,
                "me.png",
                1.0f,
                null,
                null));

        // when // then
        assertThatThrownBy(() -> userProfileReadService.getPublicProfile(requesterId, invalidId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    @DisplayName("userIds 목록으로 썸네일 URL 리스트를 조회한다")
    @Test
    void fetchThumbnailUrls_success() {
        // given
        Long id1 = 11L;
        Long id2 = 22L;

        userRepo.save(UserDocument.createUserDocument(
                id1, "u1", "+8201", LocalDate.now(),
                Gender.MALE, Role.USER, AuthProvider.KAKAO,
                true, "u1.png", 0.1f, null, null));

        userRepo.save(UserDocument.createUserDocument(
                id2, "u2", "+8202", LocalDate.now(),
                Gender.FEMALE, Role.USER, AuthProvider.NAVER,
                true, "u2.png", 0.2f, null, null));

        // when
        var urls = userProfileReadService.fetchThumbnailUrls(List.of(id1, id2));

        // then
        assertThat(urls).containsExactlyInAnyOrder("u1.png", "u2.png");
    }

    @DisplayName("존재하지 않는 ID만 주면 썸네일 URL 리스트는 비어 있다")
    @Test
    void fetchThumbnailUrls_empty() {
        // when
        var urls = userProfileReadService.fetchThumbnailUrls(List.of(999L, 888L));

        // then
        assertThat(urls).isEmpty();
    }

    @DisplayName("writerIds 목록으로 작성자 정보(id·닉네임·성별·썸네일)를 조회한다")
    @Test
    void fetchWriterInfos_success() {
        // given
        Long w1 = 31L;
        Long w2 = 32L;

        userRepo.save(UserDocument.createUserDocument(
                w1,
                "writer1",
                "+8201012341234",
                LocalDate.now(),
                Gender.MALE,
                Role.USER,
                AuthProvider.KAKAO,
                true,
                "w1.png",
                1.0f,
                null,
                null));

        userRepo.save(UserDocument.createUserDocument(
                w2,
                "writer2",
                "+8201045674567",
                LocalDate.now(),
                Gender.FEMALE,
                Role.USER,
                AuthProvider.NAVER,
                true,
                "w2.png",
                2.0f,
                null,
                null));

        // when
        var infos = userProfileReadService.fetchWriterInfos(List.of(w1, w2));

        // then
        assertThat(infos)
                .extracting("id", "writerName", "writerGender", "writerThumbnailUrl")
                .containsExactlyInAnyOrder(
                        tuple(w1, "writer1", "MALE",   "w1.png"),
                        tuple(w2, "writer2", "FEMALE", "w2.png")
                );
    }

    @DisplayName("writerIds 중 존재하는 사용자가 없으면 빈 리스트를 반환한다")
    @Test
    void fetchWriterInfos_empty() {
        // when
        var infos = userProfileReadService.fetchWriterInfos(List.of(777L));

        // then
        assertThat(infos).isEmpty();
    }
}
