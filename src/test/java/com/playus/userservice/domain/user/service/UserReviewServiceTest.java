package com.playus.userservice.domain.user.service;

import com.playus.userservice.IntegrationTestSupport;
import com.playus.userservice.domain.user.dto.UserReviewRequest;
import com.playus.userservice.domain.user.entity.*;
import com.playus.userservice.domain.user.enums.AuthProvider;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.repository.write.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class UserReviewServiceTest extends IntegrationTestSupport {

    @Autowired private UserReviewService userReviewService;
    @Autowired private UserRepository userRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private UserTagRepository userTagRepository;

    @AfterEach
    void tearDown() {
        userTagRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("리뷰 요청이 정상적으로 처리된다(+0.01, -0.01)")
    @Test
    @Transactional
    void processReviews_success() {
        // given ─ reviewer, 대상 유저, 태그 저장
        User reviewer = userRepository.save(dummyUser("reviewer"));
        User target   = userRepository.save(dummyUser("target"));
        Tag  tag      = tagRepository.save(Tag.create("시간 약속을 잘 지켜요"));

        List<UserReviewRequest> reqs = List.of(
                new UserReviewRequest(target.getId(), tag.getId(), true),   // +0.01, 태그 저장
                new UserReviewRequest(target.getId(), tag.getId(), false)   // -0.01, 태그 저장 X
        );

        // when
        List<UserReviewRequest> resp = userReviewService.processReviews(reviewer.getId(), reqs);

        // then
        assertThat(resp).hasSize(2);

        // 점수 +0.01 후 -0.01 → 원점
        User refreshed = userRepository.findById(target.getId()).get();
        assertThat(refreshed.getUserScore()).isEqualTo(User.DEFAULT_SCORE);
        assertThat(userTagRepository.findAll()).hasSize(1);
    }

    @DisplayName("존재하지 않는 reviewerId 이면 예외 발생")
    @Test
    void processReviews_reviewerNotFound() {
        // given
        long invalidReviewerId = 999L;

        List<UserReviewRequest> reqs = List.of(
                new UserReviewRequest(1L, 1L, true)
        );

        // when / then
        assertThatThrownBy(() ->
                userReviewService.processReviews(invalidReviewerId, reqs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("리뷰 요청 유저가 존재하지 않습니다");
    }

    @DisplayName("리뷰 대상 유저가 존재하지 않으면 예외 발생")
    @Test
    void processReviews_targetNotFound() {
        // given
        User reviewer = userRepository.save(dummyUser("review"));
        Tag  tag      = tagRepository.save(Tag.create("매너가 좋아요"));

        List<UserReviewRequest> reqs = List.of(
                new UserReviewRequest(999L, tag.getId(), true)   // 잘못된 targetId
        );

        // when / then
        assertThatThrownBy(() ->
                userReviewService.processReviews(reviewer.getId(), reqs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("리뷰 대상 유저가 존재하지 않습니다");
    }

    @DisplayName("positive=true 인데 tagId 가 존재하지 않으면 예외 발생")
    @Test
    void processReviews_tagNotFound() {
        // given
        User reviewer = userRepository.save(dummyUser("review"));
        User target   = userRepository.save(dummyUser("target"));

        List<UserReviewRequest> reqs = List.of(
                new UserReviewRequest(target.getId(), 999L, true)  // 잘못된 tagId
        );

        // when / then
        assertThatThrownBy(() ->
                userReviewService.processReviews(reviewer.getId(), reqs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 태그입니다");
    }

    // ────────────────────────── 헬퍼 ──────────────────────────
    private User dummyUser(String nick) {
        return User.create(
                nick,
                "010-1234-5678",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                Role.USER,
                AuthProvider.KAKAO,
                "http://thumb.url/" + nick
        );
    }
}
