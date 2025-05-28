package com.playus.userservice.domain.user.repository.write;

import com.playus.userservice.domain.user.entity.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTagRepository extends JpaRepository<UserTag, Long> {
    boolean existsByUserIdAndTagId(Long userId, Long tagId);
}

