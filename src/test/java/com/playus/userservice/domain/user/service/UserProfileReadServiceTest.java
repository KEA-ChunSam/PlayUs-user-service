package com.playus.userservice.domain.user.service;

import com.playus.userservice.IntegrationTestSupport;
import com.playus.userservice.domain.user.document.FavoriteTeamDocument;
import com.playus.userservice.domain.user.document.UserDocument;
import com.playus.userservice.domain.user.dto.profile.UserProfileResponse;
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
}
