package com.playus.userservice.domain.user.repository.write;

import com.playus.userservice.domain.user.entity.FavoriteTeam;
import com.playus.userservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface FavoriteTeamRepository extends JpaRepository<FavoriteTeam, Long> {
    Optional<FavoriteTeam> findOneByUser(User user);
    List<FavoriteTeam> findAllByUser(User user);
    void deleteByUser(User user);
    boolean existsByUserId(Long userId);
}
