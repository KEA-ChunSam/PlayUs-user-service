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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response response;


        if ("kakao".equals(registrationId)) {
            response = new KakaoResponse(oAuth2User.getAttributes());
        } else if ("naver".equals(registrationId)) {
            response = new NaverResponse(oAuth2User.getAttributes());
            log.debug("Naver raw attributes: {}", response);
        } else {
            OAuth2Error error = new OAuth2Error(
                    "unsupported_provider",
                    "지원하지 않는 OAuth2 공급자입니다: " + registrationId,
                    null
            );
            throw new OAuth2AuthenticationException(error, error.toString());
        }

        // 필수 정보 확인
        String phoneNumber = response.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            OAuth2Error error = new OAuth2Error(
                    "missing_phone",
                    "전화번호가 제공되지 않았습니다",
                    null
            );
            throw new OAuth2AuthenticationException(error, error.toString());
        }

        // 기존 사용자면 바로 CustomOAuth2User 반환
        Optional<User> existing = userRepository.findByPhoneNumber(phoneNumber);
        if (existing.isPresent()) {
            return new CustomOAuth2User(new UserDto(existing.get()));
        }

        // 신규 사용자 생성
        LocalDate birth;
        try {
            birth = LocalDate.parse(
                    response.getBirthYear() + response.getBirthday(),
                    DateTimeFormatter.ofPattern("yyyyMMdd")
            );
        } catch (Exception e) {
            birth = LocalDate.of(2000, 1, 1);
        }

        //성별
        String rawGender = response.getGender();
        Gender gender;

        // naver: M or F or U
        if ("naver".equals(registrationId)) {
            if ("F".equalsIgnoreCase(rawGender)) {
                gender = Gender.FEMALE;
            } else if ("M".equalsIgnoreCase(rawGender)) {
                gender = Gender.MALE;
            } else {
                gender = Gender.UNDEFINED;
            }
        } else {
            // Kakao: male or female
            if ("male".equalsIgnoreCase(rawGender)) {
                gender = Gender.MALE;
            } else if ("female".equalsIgnoreCase(rawGender)) {
                gender = Gender.FEMALE;
            } else {
                OAuth2Error error = new OAuth2Error(
                        "invalid_gender",
                        "유효하지 않은 성별 정보입니다: " + rawGender,
                        null
                );
                throw new OAuth2AuthenticationException(error, error.toString());
            }
        }

        User user = User.create(
                "default_nickname",
                phoneNumber,
                birth,
                gender,
                Role.USER,
                AuthProvider.KAKAO,
                "default.png"
        );
        userRepository.save(user);

        return new CustomOAuth2User(new UserDto(user));
    }
}
