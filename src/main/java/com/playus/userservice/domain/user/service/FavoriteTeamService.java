package com.playus.userservice.domain.user.service;

import com.playus.userservice.domain.user.dto.favoriteteam.FavoriteTeamRequest;
import com.playus.userservice.domain.user.dto.favoriteteam.FavoriteTeamResponse;
import com.playus.userservice.domain.user.entity.FavoriteTeam;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.enums.Team;
import com.playus.userservice.domain.user.repository.write.FavoriteTeamRepository;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteTeamService {

    private final FavoriteTeamRepository favoriteTeamRepository;
    private final UserRepository userRepository;

    /**
     * 단일 선호팀 등록 or 수정
     */
    @Transactional
    public FavoriteTeamResponse setFavoriteTeam(Long userId, FavoriteTeamRequest req) {

        // 사용자 존재 확인
        User user = userRepository.findByIdAndActivatedTrue(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 팀 ID 검증
        if (Team.fromId(req.teamId()) == null) {
            return FavoriteTeamResponse.failure("존재하지 않는 팀 ID입니다: " + req.teamId());
        }

        // 기존 선호팀이 있으면 업데이트, 없으면 생성
        return favoriteTeamRepository.findOneByUser(user)
                .map(existing -> {
                    existing.update(
                            req.teamId(),
                            Optional.ofNullable(req.displayOrder()).orElse(existing.getDisplayOrder())
                    );
                    return FavoriteTeamResponse.updated("선호팀이 정상적으로 변경되었습니다.");
                })
                .orElseGet(() -> {
                    favoriteTeamRepository.save(req.toEntity(user));
                    return FavoriteTeamResponse.created("선호팀이 정상적으로 등록되었습니다.");
                });
    }

    /**
     * 선호팀 목록 일괄 업데이트
     */
    @Transactional
    public FavoriteTeamResponse updateFavoriteTeams(Long userId, List<FavoriteTeamRequest> reqs) {
        // 사용자 존재 확인
        User user = userRepository.findByIdAndActivatedTrue(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 요청 검증 -> 비어 있으면 실패
        if (reqs == null || reqs.isEmpty()) {
            return FavoriteTeamResponse.failure("최소 한 개의 선호팀은 선택해야 합니다.");
        }

        // 우선순위 중복 및 팀 ID 검증
        Set<Integer> orders = new HashSet<>();
        for (var r : reqs) {
            if (!orders.add(r.displayOrder())) {
                return FavoriteTeamResponse.failure("우선순위가 중복되었습니다: " + r.displayOrder());
            }
            if (Team.fromId(r.teamId()) == null) {
                return FavoriteTeamResponse.failure("존재하지 않는 팀 ID입니다: " + r.teamId());
            }
        }

        // 기존 선호팀 삭제 후 전체 다시 저장
        favoriteTeamRepository.deleteByUser(user);
        favoriteTeamRepository.flush();
        List<FavoriteTeam> entities = reqs.stream()
                .map(r -> r.toEntity(user))
                .collect(Collectors.toList());
        favoriteTeamRepository.saveAll(entities);

        return FavoriteTeamResponse.updated("선호팀 목록이 정상적으로 저장되었습니다.");
    }
}
