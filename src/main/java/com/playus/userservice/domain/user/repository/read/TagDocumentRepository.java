package com.playus.userservice.domain.user.repository.read;

import com.playus.userservice.domain.user.document.TagDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TagDocumentRepository extends MongoRepository<TagDocument, Long> {
    List<TagDocument> findByIdIn(List<Long> ids);
}