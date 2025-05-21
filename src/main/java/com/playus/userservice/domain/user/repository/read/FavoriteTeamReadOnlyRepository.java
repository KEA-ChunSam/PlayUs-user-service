package com.playus.userservice.domain.user.repository.read;

import com.playus.userservice.domain.user.document.FavoriteTeamDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FavoriteTeamReadOnlyRepository extends MongoRepository<FavoriteTeamDocument, Long> {

    // 사용자별 선호팀 목록 -> displayOrder 순으로 반환
    List<FavoriteTeamDocument> findAllByUserIdOrderByDisplayOrderAsc(Long userId);
}
