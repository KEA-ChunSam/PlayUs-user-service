package com.playus.userservice.domain.user.service;

import com.playus.userservice.domain.user.dto.review.UserTagSummaryResponse;
import com.playus.userservice.domain.user.document.UserTagDocument;
import com.playus.userservice.domain.user.document.TagDocument;
import com.playus.userservice.domain.user.repository.read.TagDocumentRepository;
import com.playus.userservice.domain.user.repository.read.UserReadOnlyRepository;
import com.playus.userservice.domain.user.repository.read.UserTagDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserTagReadService {

    private final UserTagDocumentRepository userTagRepository;
    private final TagDocumentRepository tagRepository;
    private final UserReadOnlyRepository userReadOnlyRepository;

    private static final long NoTAG_ID = 2L;

    public UserTagSummaryResponse getUserTagSummary(Long userId, Long targetId) {

        userReadOnlyRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        userReadOnlyRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "조회하려는 사용자를 찾을 수 없습니다."));

        long totalCount = userTagRepository.countByUserId(targetId);
        long negativeTagCount = userTagRepository.countByUserIdAndTagId(targetId, NoTAG_ID);
        long positiveTagCount = totalCount - negativeTagCount;

        // 좋은태그 가진 목록 가져와서 빈도 계산
        List<UserTagDocument> docs = userTagRepository.findByUserIdAndTagIdNot(targetId, NoTAG_ID);

        Map<Long, Long> freq = docs.stream()
                .collect(Collectors.groupingBy(UserTagDocument::getTagId, Collectors.counting()));

        // 빈도순 상위 3개 tagId
        List<Long> topTagIds = freq.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        // 태그 이름 조회
        List<TagDocument> tagDocs = tagRepository.findByIdIn(topTagIds);

        // id → name 매핑 후 순서 보존
        Map<Long, String> idToName = tagDocs.stream()
                .collect(Collectors.toMap(TagDocument::getId, TagDocument::getTagName));

        List<String> topTagNames = topTagIds.stream()
                .map(idToName::get)
                .filter(Objects::nonNull)
                .toList();

        return new UserTagSummaryResponse(totalCount, positiveTagCount, topTagNames);
    }
}
