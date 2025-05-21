package com.playus.userservice.domain.user.service;

import com.playus.userservice.domain.user.document.UserDocument;
import com.playus.userservice.domain.user.dto.UserInfoResponse;
import com.playus.userservice.domain.user.dto.partyuser.PartyApplicantsInfoFeignResponse;
import com.playus.userservice.domain.user.dto.partyuser.PartyWriterInfoFeignResponse;
import com.playus.userservice.domain.user.dto.profile.FavoriteTeamDto;
import com.playus.userservice.domain.user.dto.profile.UserProfileResponse;
import com.playus.userservice.domain.user.dto.profile.UserPublicProfileResponse;
import com.playus.userservice.domain.user.repository.read.FavoriteTeamReadOnlyRepository;
import com.playus.userservice.domain.user.repository.read.UserReadOnlyRepository;
import com.playus.userservice.global.util.AgeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileReadService {

    private final UserReadOnlyRepository userRepository;
    private final FavoriteTeamReadOnlyRepository favoriteTeamRepository;

    // 내 프로필
    public UserProfileResponse getProfile(Long userId) {

        // 요청자 존재 확인
        UserDocument user = fetchUser(userId);
        List<FavoriteTeamDto> teams = fetchTeams(userId);
        return toPrivateResponse(user, teams);
    }

    // 다른 사람 프로필
    public UserPublicProfileResponse getPublicProfile(Long userId, Long targetUserId) {

        // 요청자 존재 확인
        fetchUser(userId);

        // 조회 대상 사용자 존재 확인
        UserDocument targetUser = fetchUser(targetUserId);
        List<FavoriteTeamDto> teams = fetchTeams(targetUserId);
        return toPublicResponse(targetUser, teams);
    }

    // 다른 사람 프로필 (nickname, profileImageUrl)
    public UserInfoResponse getPublicProfileOnlyNicknameAndImageUrl(Long targetUserId) {

        // 요청상 요청자 존재 확인x

        // 조회 대상 사용자 존재 확인
        UserDocument targetUser = fetchUser(targetUserId);
        return new UserInfoResponse(targetUser.getNickname(), targetUser.getThumbnailURL());

    }

    /**
     * 직관팟 섬네일 조회:
     * userIds 목록을 받아 각 사용자의 썸네일 URL 반환
     */

    public List<String> fetchThumbnailUrls(List<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .map(UserDocument::getThumbnailURL)
                .toList();
    }

    /**
     * 직관팟 작성자 정보 조회:
     * writerIds 목록을 받아 각 작성자의 id, 닉네임, 성별, 썸네일 URL 반환
     */
    public List<PartyWriterInfoFeignResponse> fetchWriterInfos(List<Long> writerIds) {
        return userRepository.findAllById(writerIds).stream()
                .map(this::toFeignResponse)
                .toList();
    }

    /**
     * 직관팟 참가자 정보 조회:
     * userIds 목록을 받아 각 작성자의 id, 닉네임, 성별, 연령대, 썸네일 URL 반환
     */
    public List<PartyApplicantsInfoFeignResponse> fetchApplicantsInfos(List<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .map(this::toApplicantsFeignResponse)
                .toList();
    }

    private UserDocument fetchUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private List<FavoriteTeamDto> fetchTeams(Long userId) {
        return favoriteTeamRepository
                .findAllByUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(doc -> FavoriteTeamDto.builder()
                        .teamId(doc.getTeamId())
                        .displayOrder(doc.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }

    private UserProfileResponse toPrivateResponse(UserDocument user, List<FavoriteTeamDto> teams) {
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

    private UserPublicProfileResponse toPublicResponse(UserDocument user, List<FavoriteTeamDto> teams) {
        return UserPublicProfileResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .thumbnailURL(user.getThumbnailURL())
                .userScore(user.getUserScore())
                .favoriteTeams(teams)
                .build();
    }

    private PartyWriterInfoFeignResponse toFeignResponse(UserDocument doc) {
        return PartyWriterInfoFeignResponse.of(
                doc.getId(),
                doc.getNickname(),
                doc.getGender().name(),
                doc.getThumbnailURL()
        );
    }

    private PartyApplicantsInfoFeignResponse toApplicantsFeignResponse(UserDocument doc) {
        int age = AgeUtils.calculateAge(doc.getBirth().atStartOfDay());
        return PartyApplicantsInfoFeignResponse.of(
                doc.getId(),
                doc.getNickname(),
                age,
                doc.getThumbnailURL()
        );
    }

}
