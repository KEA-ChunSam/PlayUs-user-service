package com.playus.userservice.domain.user.service;

import com.playus.userservice.domain.user.document.FavoriteTeamDocument;
import com.playus.userservice.domain.user.document.UserDocument;
import com.playus.userservice.domain.user.dto.profile.FavoriteTeamDto;
import com.playus.userservice.domain.user.dto.profile.UserProfileResponse;
import com.playus.userservice.domain.user.repository.read.FavoriteTeamReadOnlyRepository;
import com.playus.userservice.domain.user.repository.read.UserReadOnlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileReadService {

    private final UserReadOnlyRepository userRepository;
    private final FavoriteTeamReadOnlyRepository favoriteTeamRepository;

    public UserProfileResponse getProfile(Long userId) {

        UserDocument user = userRepository.findById(userId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        List<FavoriteTeamDto> teams = favoriteTeamRepository
                .findAllByUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(this::toDto)
                .collect(toList());

        return toResponse(user, teams);
    }


    private FavoriteTeamDto toDto(FavoriteTeamDocument doc) {
        return FavoriteTeamDto.builder()
                .teamId(doc.getTeamId())
                .displayOrder(doc.getDisplayOrder())
                .build();
    }

    private UserProfileResponse toResponse(UserDocument user, List<FavoriteTeamDto> teams) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .birth(user.getBirth())
                .gender(user.getGender())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .activated(user.isActivated())
                .thumbnailURL(user.getThumbnailURL())
                .userScore(user.getUserScore())
                .blockOff(user.getBlockOff())
                .favoriteTeams(teams)
                .build();
    }
}
