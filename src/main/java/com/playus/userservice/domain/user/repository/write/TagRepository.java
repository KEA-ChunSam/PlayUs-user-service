package com.playus.userservice.domain.user.repository.write;

import com.playus.userservice.domain.user.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}

