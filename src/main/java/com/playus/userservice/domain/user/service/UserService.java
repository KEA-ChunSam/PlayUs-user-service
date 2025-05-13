package com.playus.userservice.domain.user.service;

import com.playus.userservice.domain.user.dto.NicknameDto;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public NicknameDto.NicknameResponse updateNickname(Long userId, NicknameDto.NicknameRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        user.updateNickname(req.getNickname());
        // (managed entity이므로 save() 생략 가능)

        return new NicknameDto.NicknameResponse(true, "닉네임이 성공적으로 변경되었습니다.");
    }
}