package com.playus.userservice.domain.user.service;

import com.playus.userservice.IntegrationTestSupport;
import com.playus.userservice.domain.user.document.TagDocument;
import com.playus.userservice.domain.user.document.UserDocument;
import com.playus.userservice.domain.user.document.UserTagDocument;
import com.playus.userservice.domain.user.dto.review.UserTagSummaryResponse;
import com.playus.userservice.domain.user.repository.read.TagDocumentRepository;
import com.playus.userservice.domain.user.repository.read.UserReadOnlyRepository;
import com.playus.userservice.domain.user.repository.read.UserTagDocumentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class UserTagReadServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserTagReadService userTagReadService;

    @Autowired
    private UserReadOnlyRepository userRepo;

    @Autowired
    private UserTagDocumentRepository userTagRepo;

    @Autowired
    private TagDocumentRepository tagRepo;

    @AfterEach
    void tearDown() {
        userTagRepo.deleteAll();
        tagRepo.deleteAll();
        userRepo.deleteAll();
    }

    @DisplayName("조회자와 대상 모두 존재하면 태그 요약을 정상 계산한다")
    @Test
    void getUserTagSummary_success() {
        // given
        Long viewerId = 1L;
        Long targetId = 2L;
        userRepo.save(UserDocument.createUserDocument(
                viewerId,
                "viewer",
                "+8201000",
                LocalDate.now(),
                null, null, null, true, null, 0f, null, null
        ));
        userRepo.save(UserDocument.createUserDocument(
                targetId,
                "target",
                "+8201001",
                LocalDate.now(),
                null, null, null, true, null, 0f, null, null
        ));

        userTagRepo.save(UserTagDocument.createUserTagDocument(1L, targetId, 1L));
        userTagRepo.save(UserTagDocument.createUserTagDocument(2L, targetId, 1L));
        userTagRepo.save(UserTagDocument.createUserTagDocument(3L, targetId, 2L));
        userTagRepo.save(UserTagDocument.createUserTagDocument(4L, targetId, 3L));
        userTagRepo.save(UserTagDocument.createUserTagDocument(5L, targetId, 99L));

        tagRepo.save(TagDocument.createTagDocument(1L, "A"));
        tagRepo.save(TagDocument.createTagDocument(2L, "B"));
        tagRepo.save(TagDocument.createTagDocument(3L, "C"));

        // when
        UserTagSummaryResponse result = userTagReadService.getUserTagSummary(viewerId, targetId);

        // then
        assertThat(result.totalCount()).isEqualTo(5);
        assertThat(result.positiveTagCount()).isEqualTo(4);
        assertThat(result.topTags()).containsExactly("A", "C");
    }

    @DisplayName("조회자가 없으면 404 ResponseStatusException")
    @Test
    void getUserTagSummary_viewerNotFound() {
        Long invalidViewer = 999L;
        Long targetId = 2L;
        userRepo.save(UserDocument.createUserDocument(
                targetId,
                "target",
                "+8201001",
                LocalDate.now(),
                null, null, null, true, null, 0f, null, null
        ));

        assertThatThrownBy(() ->
                userTagReadService.getUserTagSummary(invalidViewer, targetId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException e = (ResponseStatusException) ex;
                    assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
                    assertThat(e.getReason()).isEqualTo("사용자를 찾을 수 없습니다.");
                });
    }

    @DisplayName("조회 대상 사용자가 없으면 404 ResponseStatusException")
    @Test
    void getUserTagSummary_targetNotFound() {
        Long viewerId = 1L;
        Long invalidTarget = 999L;
        userRepo.save(UserDocument.createUserDocument(
                viewerId,
                "viewer",
                "+8201000",
                LocalDate.now(),
                null, null, null, true, null, 0f, null, null
        ));

        assertThatThrownBy(() ->
                userTagReadService.getUserTagSummary(viewerId, invalidTarget))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException e = (ResponseStatusException) ex;
                    assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
                    assertThat(e.getReason()).isEqualTo("조회하려는 사용자를 찾을 수 없습니다.");
                });
    }
}
