package com.playus.userservice.domain.user.repository.read;

import com.playus.userservice.domain.user.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserReadOnlyRepository extends MongoRepository<User, Long> {
}
