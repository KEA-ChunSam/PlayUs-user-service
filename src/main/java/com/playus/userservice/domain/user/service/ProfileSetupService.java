package com.playus.userservice.domain.user.service;

import com.playus.userservice.domain.user.dto.FavoriteTeamDto.*;
import com.playus.userservice.domain.user.dto.NicknameDto.NicknameRequest;
import com.playus.userservice.domain.user.dto.ProfileSetupDto.UserRegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileSetupService {

    private final UserService userService;
    private final FavoriteTeamService favoriteTeamService;

    @Transactional
    public UserRegisterResponse setupProfile(
            Long userId,
            Long favoriteTeamId,
            String nickname
            ) {

        //(초기 profile은 displayOrder=1 고정)
        favoriteTeamService.setFavoriteTeam(userId, new FavoriteTeamRequest(favoriteTeamId, 1));

        userService.updateNickname(userId, new NicknameRequest(nickname));

        return new UserRegisterResponse(true, "프로필이 정상적으로 설정되었습니다.");
    }
}