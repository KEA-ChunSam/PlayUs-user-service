package com.playus.userservice.domain.user.service;

import com.playus.userservice.domain.user.dto.review.UserReviewRequest;
import com.playus.userservice.domain.user.entity.*;
import com.playus.userservice.domain.user.repository.write.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserReviewService {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final UserTagRepository userTagRepository;

    private static final float SCORE_STEP = 0.001f;
    private static final float MAX_SCORE = 1.0f;
    private static final float MIN_SCORE = 0.0f;

    @Transactional
    public List<UserReviewRequest> processReviews(Long reviewerId, List<UserReviewRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "리뷰 요청 목록이 비어있습니다.");
        }

        // 자기 자신 리뷰 방지
        boolean selfReview = requests.stream()
                .anyMatch(req -> req.userId().equals(reviewerId));
        if (selfReview) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신에 대한 리뷰는 불가능합니다.");
        }

        userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 요청 유저가 존재하지 않습니다. id=" + reviewerId));


        for (UserReviewRequest dto : requests) {
            User user = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new IllegalArgumentException("리뷰 대상 유저가 존재하지 않습니다. id=" + dto.userId()));

            Tag tag = tagRepository.findById(dto.tagId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다. id=" + dto.tagId()));

            float currentScore = user.getUserScore();
            float delta = dto.positive() ? SCORE_STEP : -SCORE_STEP;
            float newScore = currentScore + delta;

            // 상한
            if (newScore > MAX_SCORE) {
                newScore = MAX_SCORE;
            }
            // 하한
            else if (newScore < MIN_SCORE) {
                newScore = MIN_SCORE;
            }

            user.updateUserScore(newScore);
            userTagRepository.save(UserTag.create(user, tag));
        }

        return requests;

    }

}
