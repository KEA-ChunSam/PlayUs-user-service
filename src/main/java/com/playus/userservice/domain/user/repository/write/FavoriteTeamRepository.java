package com.playus.userservice.domain.user.repository.write;

import com.playus.userservice.domain.user.entity.FavoriteTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteTeamRepository extends JpaRepository<FavoriteTeam,Long> {
    Optional<FavoriteTeam> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
