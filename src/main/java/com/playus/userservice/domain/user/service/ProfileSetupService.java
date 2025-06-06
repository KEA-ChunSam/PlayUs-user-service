package com.playus.userservice.domain.user.service;

import com.playus.userservice.domain.user.dto.nickname.ProfileUpdateRequest;
import com.playus.userservice.domain.user.dto.favoriteteam.FavoriteTeamRequest;
import com.playus.userservice.domain.user.dto.presigned.PresignedUrlForSaveImageRequest;
import com.playus.userservice.domain.user.dto.presigned.PresignedUrlForSaveImageResponse;
import com.playus.userservice.domain.user.dto.profilesetup.UserRegisterResponse;
import com.playus.userservice.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileSetupService {

    private final UserService userService;
    private final FavoriteTeamService favoriteTeamService;
    private final S3Service s3Service;

    @Transactional
    public UserRegisterResponse setupProfile(
            Long userId,
            Long favoriteTeamId,
            String nickname,
            String thumbnailURL
    ) {
        // (초기 profile은 displayOrder=1 고정)
        favoriteTeamService.setFavoriteTeam(
                userId,
                new FavoriteTeamRequest(favoriteTeamId, 1)
        );

        userService.updateProfile(userId, new ProfileUpdateRequest(nickname, thumbnailURL));

        return new UserRegisterResponse(true, "프로필이 정상적으로 설정되었습니다.");
    }

    public PresignedUrlForSaveImageResponse generatePresignedUrlForSaveImage(
            PresignedUrlForSaveImageRequest request) {

        String url = s3Service.generatePresignedUrl(request.imageFileName());
        return new PresignedUrlForSaveImageResponse(url);
    }
}
