package com.playus.userservice.domain.oauth.service;

import com.playus.userservice.domain.user.dto.*;
import com.playus.userservice.domain.oauth.dto.*;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.enums.AuthProvider;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = false)
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response response = switch (registrationId) {
            case "kakao" -> new KakaoResponse(oAuth2User.getAttributes());
            case "naver" -> new NaverResponse(oAuth2User.getAttributes());
            default -> throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider",
                            "지원하지 않는 OAuth2 공급자입니다: " + registrationId, null));
        };

        // 전화번호 필수 확인
        String phoneNumber = response.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_phone", "전화번호가 제공되지 않았습니다", null));
        }
        String normalizedPhone = normalizePhone(phoneNumber);

        // 이미 가입된 사용자 처리
        Optional<User> existing = userRepository.findByPhoneNumber(phoneNumber);
        AuthProvider currentProvider = "kakao".equals(registrationId)
                ? AuthProvider.KAKAO
                : AuthProvider.NAVER;

        if (existing.isPresent()) {
            User user = existing.get();

            // 공급자가 일치 → 바로 로그인
            if (user.getAuthProvider() == currentProvider) {
                return new CustomOAuth2User(new UserDto(user));
            }

            // 공급자가 다름 → 오류
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("provider_mismatch",
                            "이 전화번호는 이미 " + user.getAuthProvider()
                                    + " 계정으로 가입되어 있습니다.", null));
        }

        // 신규 사용자 생성
        LocalDate birth = parseBirth(response.getBirthYear(), response.getBirthday());

        Gender gender = mapGender(registrationId, response.getGender());

        User user = User.create(
                "default_nickname",
                normalizedPhone,
                birth,
                gender,
                Role.USER,
                currentProvider,
                "default.png"
        );
        userRepository.save(user);

        return new CustomOAuth2User(new UserDto(user));
    }

    // 전화번호 정규화 (+821012345678 과 같은 E.164 형식으로 반환)
    private String normalizePhone(String raw) {

        String digits = raw.replaceAll("[^+\\d]", "");

        if (digits.startsWith("+82")) {
            return digits;
        }
        if (digits.startsWith("0")) {
            return "+82" + digits.substring(1);
        }
        // 국내번호 가정 -> +82 붙임 (필요 시 정책 조정)
        return "+82" + digits;
    }

    // 생년월일
    private LocalDate parseBirth(String year, String dayMonth) {
        if (year == null || dayMonth == null) return LocalDate.of(2000, 1, 1);

        // 하이픈 슬래시 제거 (ex: 10_01 → 1001)
        String mmdd = dayMonth.replaceAll("[^\\d]", "");
        if (mmdd.length() != 4) return LocalDate.of(2000, 1, 1);

        try {
            return LocalDate.parse(year + mmdd, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return LocalDate.of(2000, 1, 1);
        }
    }

    // 성별 매핑
    private Gender mapGender(String registrationId, String raw) {

        // naver: M or F or U
        if ("naver".equals(registrationId)) {
            return switch (raw == null ? "" : raw.toUpperCase()) {
                case "F" -> Gender.FEMALE;
                case "M" -> Gender.MALE;
                default -> Gender.UNDEFINED;
            };
        } else { // kakao : male or female
            if ("male".equalsIgnoreCase(raw)) return Gender.MALE;
            if ("female".equalsIgnoreCase(raw)) return Gender.FEMALE;
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_gender",
                            "유효하지 않은 성별 정보입니다: " + raw, null));
        }
    }
}
