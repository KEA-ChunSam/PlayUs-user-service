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

    /**
     * OAuth2UserRequest < resource server에서 온 유저 정보
     * @param userRequest
     * @return
     * @throws OAuth2AuthenticationException
     */

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.debug("OAuth2 user attributes: {}", oAuth2User.getAttributes());

        // kakao에서 온 건지, naver에서 온 건지 확인
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response response = null;


        if ("kakao".equals(registrationId)) {
            response = new KakaoResponse(oAuth2User.getAttributes());
        }

        /*
        else if ("naver".equals(registrationId)) {
            response = new NaverResponse(oAuth2User.getAttributes());
        }
         */

        else {
            OAuth2Error error = new OAuth2Error(
                    "unsupported_provider",
                    "지원하지 않는 OAuth2 공급자입니다: " + registrationId,
                    null
            );
            throw new OAuth2AuthenticationException(error, error.toString());
        }


        // 전화번호 기반 중복 체크
        String phoneNumber = response.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            OAuth2Error error = new OAuth2Error(
                    "missing_phone",
                    "전화번호가 제공되지 않았습니다",
                    null
            );
            throw new OAuth2AuthenticationException(error, error.toString());
        }
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            OAuth2Error error = new OAuth2Error(
                    "duplicated_user",
                    "이미 등록된 전화번호입니다",
                    null
            );
            throw new OAuth2AuthenticationException(error, error.toString());
        }

        // 신규 유저 생성 로직
        LocalDate birth;
        try {
            birth = LocalDate.parse(
                    response.getBirthYear() + response.getBirthday(),
                    DateTimeFormatter.ofPattern("yyyyMMdd")
            );
        } catch (Exception e) {
            birth = LocalDate.of(2000, 1, 1);
        }

        String respGender = response.getGender();
        Gender gender;
        try {
            gender = Gender.valueOf(respGender.toUpperCase());  // male→MALE, female→FEMALE
        } catch (IllegalArgumentException | NullPointerException ex) {
            OAuth2Error error = new OAuth2Error("invalid_gender", "유효하지 않은 성별 정보입니다", null);
            throw new OAuth2AuthenticationException(error, error.toString());
        }
        User user = User.create(
                "default_nickname",            // nickname 기본값, 나중에 프론트에서 입력
                phoneNumber,
                birth,
                gender,
                Role.USER,
                AuthProvider.KAKAO,
                "default.png"                  // 썸네일 기본값, 나중에 프론트에서 입력
        );

        userRepository.save(user);
        UserDto userDto = new UserDto(user);
        return new CustomOAuth2User(userDto);
    }
}
