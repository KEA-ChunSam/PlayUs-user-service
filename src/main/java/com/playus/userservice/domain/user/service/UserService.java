package com.playus.userservice.domain.user.service;

import com.playus.userservice.domain.oauth.service.AuthService;
import com.playus.userservice.domain.user.dto.withdraw.UserWithdrawResponse;
import com.playus.userservice.domain.user.dto.nickname.NicknameRequest;
import com.playus.userservice.domain.user.dto.nickname.NicknameResponse;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Transactional
    public NicknameResponse updateNickname(Long userId, NicknameRequest req) {

        // 사용자 조회
        User user = userRepository.findByIdAndActivatedTrue(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        String newNickname = req.nickname();

        // 닉네임 중복 확인 (db에 존재 & 본인 닉네임 아님)
        if (userRepository.existsByNickname(newNickname)
                && !newNickname.equals(user.getNickname())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }
        user.updateNickname(newNickname);
        return new NicknameResponse(true, "닉네임이 성공적으로 변경되었습니다.");
    }

    @Transactional
    public void updateImage(Long userId, String thumbnailURL) {
        User user = userRepository.findByIdAndActivatedTrue(userId)
                .orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        user.updateImage(thumbnailURL);
    }

    @Transactional
    public UserWithdrawResponse withdraw(Long userId, HttpServletRequest req, HttpServletResponse res) {

        User user = userRepository.findByIdAndActivatedTrue(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        user.withdrawAccount();   // activated = false 로 변경
        authService.logout(req, res);
        return new UserWithdrawResponse(true, "회원 탈퇴가 완료되었습니다.");
    }
}
