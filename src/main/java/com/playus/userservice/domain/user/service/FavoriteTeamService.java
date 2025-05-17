package com.playus.userservice.domain.user.service;

import com.playus.userservice.domain.user.dto.FavoriteTeamDto;
import com.playus.userservice.domain.user.entity.FavoriteTeam;
import com.playus.userservice.domain.user.enums.Team;
import com.playus.userservice.domain.user.repository.write.FavoriteTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FavoriteTeamService {

    private final FavoriteTeamRepository favoriteTeamRepository;

    @Transactional
    public FavoriteTeamDto.FavoriteTeamResponse setFavoriteTeam(Long userId, FavoriteTeamDto.FavoriteTeamRequest req) {

        // 팀 ID 검증 (Enum 기반)
        Team team = Team.fromId(req.getTeamId());
        if (team == null) {
            return new FavoriteTeamDto.FavoriteTeamResponse(false, "존재하지 않는 팀 ID입니다.");
        }

        // 기존 선호팀 여부 확인 후 update / insert
        return favoriteTeamRepository.findByUserId(userId)
                .map(ft -> {
                    ft.update(req.getTeamId(),
                            req.getDisplayOrder() != null ? req.getDisplayOrder() : ft.getDisplayOrder());
                    return new FavoriteTeamDto.FavoriteTeamResponse(true, "선호팀이 정상적으로 변경되었습니다.");
                })
                .orElseGet(() -> {
                    int order = req.getDisplayOrder() != null ? req.getDisplayOrder() : 1;
                    favoriteTeamRepository.save(
                            FavoriteTeam.create(userId, req.getTeamId(), order));
                    return new FavoriteTeamDto.FavoriteTeamResponse(true, "선호팀이 정상적으로 등록되었습니다.");
                });
    }

    @Transactional
    public FavoriteTeamDto.FavoriteTeamResponse updateFavoriteTeams(Long userId, List<FavoriteTeamDto.FavoriteTeamRequest> reqs) {

        if (reqs == null || reqs.isEmpty()) {
            return new FavoriteTeamDto.FavoriteTeamResponse(false, "최소 한 개의 선호팀은 선택해야 합니다.");
        }

        // 우선순위 중복 체크
        Set<Integer> orders = new HashSet<>();
        for (FavoriteTeamDto.FavoriteTeamRequest r : reqs) {
            if (!orders.add(r.getDisplayOrder())) {
                return new FavoriteTeamDto.FavoriteTeamResponse(false, "우선순위가 중복되었습니다: " + r.getDisplayOrder());
            }
            // 팀 ID 유효성 검증
            if (Team.fromId(r.getTeamId()) == null) {
                return new FavoriteTeamDto.FavoriteTeamResponse(false, "존재하지 않는 팀 ID입니다: " + r.getTeamId());
            }
        }

        favoriteTeamRepository.deleteByUserId(userId);
        // 새 목록으로 모두 저장
        for (FavoriteTeamDto.FavoriteTeamRequest r : reqs) {
            favoriteTeamRepository.save(
                    FavoriteTeam.create(userId, r.getTeamId(), r.getDisplayOrder())
            );
        }
        return new FavoriteTeamDto.FavoriteTeamResponse(true, "선호팀 목록이 정상적으로 저장되었습니다.");
    }
}
