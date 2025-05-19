package com.playus.userservice.domain.user.repository.read;

import com.playus.userservice.domain.user.document.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserReadOnlyRepository extends MongoRepository<UserDocument, Long> {
}
