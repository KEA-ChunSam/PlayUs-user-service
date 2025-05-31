package com.playus.userservice.domain.user.dto.review;

import java.util.List;

public record UserTagSummaryResponse(
        long totalCount,       // 전체 userTag 수
        long positiveTagCount,   // No_tag(tag_id = 2)를 제외한 수
        List<String> topTags   // No_tag를 제외한 최다 빈도 태그명 (최대 3개 리턴)
) {}
