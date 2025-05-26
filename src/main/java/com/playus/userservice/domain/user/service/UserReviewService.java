package com.playus.userservice.domain.user.service;

import com.playus.userservice.domain.user.dto.UserReviewRequest;
import com.playus.userservice.domain.user.entity.*;
import com.playus.userservice.domain.user.repository.write.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserReviewService {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final UserTagRepository userTagRepository;

    private static final float score = 0.01f;

    @Transactional
    public List<UserReviewRequest> processReviews(Long reviewerId, List<UserReviewRequest> requests) {

        userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 요청 유저가 존재하지 않습니다. id=" + reviewerId));


        for (UserReviewRequest dto : requests) {
            User user = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new IllegalArgumentException("리뷰 대상 유저가 존재하지 않습니다. id=" + dto.userId()));

            Tag tag = tagRepository.findById(dto.tagId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다. id=" + dto.tagId()));

            if (dto.positive()) {
                user.updateUserScore(user.getUserScore() + score);
            } else {
                user.updateUserScore(user.getUserScore() - score);
            }
            userTagRepository.save(UserTag.create(user, tag));
        }

        return requests;

    }

}
