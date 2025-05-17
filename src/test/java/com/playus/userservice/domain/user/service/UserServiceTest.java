package com.playus.userservice.domain.user.service;

import com.playus.userservice.IntegrationTestSupport;
import com.playus.userservice.domain.user.dto.NicknameDto.NicknameRequest;
import com.playus.userservice.domain.user.dto.NicknameDto.NicknameResponse;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.enums.AuthProvider;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class UserServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @DisplayName("닉네임을 성공적으로 업데이트한다")
    @Test
    void updateNickname_success() {
        // given
        User savedUser = userRepository.save(
                User.create(
                        "oldNickname",
                        "010-1234-5678",
                        LocalDate.of(1990, 1, 1),
                        Gender.MALE,
                        Role.USER,
                        AuthProvider.KAKAO,
                        "http://example.com/thumb.jpg"
                )
        );
        Long userId = savedUser.getId();
        NicknameRequest req = new NicknameRequest("  newNickname  ");

        // when
        NicknameResponse resp = userService.updateNickname(userId, req);

        // then
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getMessage()).isEqualTo("닉네임이 성공적으로 변경되었습니다.");

        User updated = userRepository.findById(userId).orElseThrow();
        assertThat(updated.getNickname()).isEqualTo("newNickname");
    }

    @DisplayName("존재하지 않는 유저의 닉네임 업데이트 시 NOT_FOUND 예외 발생")
    @Test
    void updateNickname_userNotFound() {
        // given
        Long invalidUserId = 999L;
        NicknameRequest req = new NicknameRequest("nickname");

        // when / then
        assertThatThrownBy(() -> userService.updateNickname(invalidUserId, req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> {
                    ResponseStatusException ex = (ResponseStatusException) e;
                    assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason()).isEqualTo("사용자를 찾을 수 없습니다.");
                });
    }

    @DisplayName("이미 존재하는 닉네임으로 변경 시 CONFLICT 예외 발생")
    @Test
    void updateNickname_conflict() {
        // given 두 명 저장
        userRepository.save(
                User.create(
                        "test1",
                        "010-0000-0001",
                        LocalDate.of(1990, 1, 1),
                        Gender.FEMALE,
                        Role.USER,
                        AuthProvider.KAKAO,
                        "http://example.com/a.jpg"
                )
        );
        User other = userRepository.save(
                User.create(
                        "test2",
                        "010-0000-0002",
                        LocalDate.of(1991, 2, 2),
                        Gender.MALE,
                        Role.USER,
                        AuthProvider.KAKAO,
                        "http://example.com/b.jpg"
                )
        );
        NicknameRequest req = new NicknameRequest("existNickname");

        // when / then
        assertThatThrownBy(() -> userService.updateNickname(other.getId(), req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> {
                    ResponseStatusException ex = (ResponseStatusException) e;
                    assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
                    assertThat(ex.getReason()).isEqualTo("이미 사용 중인 닉네임입니다.");
                });
    }

    @DisplayName("기존 닉네임과 동일한 값으로 변경해도 예외 없이 성공")
    @Test
    void updateNickname_sameAsCurrent() {
        // given: 유저 저장
        User user = userRepository.save(
                User.create(
                        "sameName",
                        "010-0000-0003",
                        LocalDate.of(1992, 3, 3),
                        Gender.FEMALE,
                        Role.USER,
                        AuthProvider.KAKAO,
                        "http://example.com/c.jpg"
                )
        );
        NicknameRequest req = new NicknameRequest("sameName");

        // when
        NicknameResponse resp = userService.updateNickname(user.getId(), req);

        // then
        assertThat(resp.isSuccess()).isTrue();
        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getNickname()).isEqualTo("sameName");
    }
}
