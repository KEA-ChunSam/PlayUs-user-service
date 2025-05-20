package com.playus.userservice.domain.user.service;

import com.playus.userservice.IntegrationTestSupport;
import com.playus.userservice.domain.oauth.service.AuthService;
import com.playus.userservice.domain.user.dto.UserWithdrawResponse;
import com.playus.userservice.domain.user.dto.nickname.NicknameRequest;
import com.playus.userservice.domain.user.dto.nickname.NicknameResponse;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.enums.AuthProvider;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

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
                        "test1",
                        "010-1234-5678",
                        LocalDate.of(1990, 1, 1),
                        Gender.MALE,
                        Role.USER,
                        AuthProvider.KAKAO,
                        "http://example.com/thumb.jpg"
                )
        );
        Long userId = savedUser.getId();
        NicknameRequest req = new NicknameRequest("test2");

        // when
        NicknameResponse resp = userService.updateNickname(userId, req);

        // then
        assertThat(resp.success()).isTrue();
        assertThat(resp.message()).isEqualTo("닉네임이 성공적으로 변경되었습니다.");

        User updated = userRepository.findByIdAndActivatedTrue(userId).get();
        assertThat(updated.getNickname()).isEqualTo("test2");
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
        NicknameRequest req = new NicknameRequest("test1");

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
        assertThat(resp.success()).isTrue();
        User updated = userRepository.findByIdAndActivatedTrue(user.getId()).get();
        assertThat(updated.getNickname()).isEqualTo("sameName");
    }

    @DisplayName("updateImage()는 썸네일 URL을 정상 반영한다")
    @Test
    void updateImage_success() {

        // given
        User user = userRepository.save(
                User.create("test3", "010-3333-4444",
                        LocalDate.of(2000, 3, 3),
                        Gender.MALE, Role.USER,
                        AuthProvider.KAKAO, "http://old.jpg"));
        String newUrl = "http://brand-new.jpg";

        // when
        userService.updateImage(user.getId(), newUrl);

        // then
        User refreshed = userRepository.findByIdAndActivatedTrue(user.getId()).get();
        assertThat(refreshed.getThumbnailURL()).isEqualTo(newUrl);
    }

    /**
     * @SQLDelete(sql = "UPDATE users SET activated = false WHERE id = ?")
     * @Where(clause = "activated = true") 주석 처리후 실행
     */

    @DisplayName("회원 탈퇴가 정상적으로 처리된다")
    @Test
    void withdraw_success() {

        AuthService authMock = mock(AuthService.class);
        ReflectionTestUtils.setField(userService, "authService", authMock);

        // logout()이 호출되더라도 예외 없이 통과하도록 스텁
        doNothing().when(authMock)
                .logout(any(HttpServletRequest.class), any(HttpServletResponse.class));

        // given
        User user = userRepository.save(
                User.create(
                        "testUser",
                        "010-1234-0000",
                        LocalDate.of(1990, 1, 1),
                        Gender.MALE,
                        Role.USER,
                        AuthProvider.KAKAO,
                        "http://example.com/thumb.jpg"
                )
        );
        Long userId = user.getId();

        // when
        HttpServletRequest req = new MockHttpServletRequest();
        HttpServletResponse res = new MockHttpServletResponse();
        UserWithdrawResponse resp = userService.withdraw(userId, req, res);

        // then
        assertThat(resp.success()).isTrue();
        assertThat(resp.message()).isEqualTo("회원 탈퇴가 완료되었습니다.");

        User updated = userRepository.findById(userId).get();
        assertThat(updated.isActivated()).isFalse();
    }

}
