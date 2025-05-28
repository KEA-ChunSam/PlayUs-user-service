package com.playus.userservice.domain.user.repository.read;

import com.playus.userservice.domain.user.document.UserTagDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserTagDocumentRepository extends MongoRepository<UserTagDocument, Long> {
    long countByUserId(Long userId);
    long countByUserIdAndTagId(Long userId, Long tagId);
    List<UserTagDocument> findByUserIdAndTagIdNot(Long userId, Long tagId);
}
