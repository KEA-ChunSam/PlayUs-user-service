package com.playus.userservice.domain.oauth.service;

import com.playus.userservice.domain.user.dto.*;
import com.playus.userservice.domain.oauth.dto.*;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.enums.AuthProvider;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
        System.out.println(oAuth2User);

        // naver에서 온 건지, google에서 온 건지 등 확인
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response response = null;
        if ("naver".equals(registrationId)) {
            response = new NaverResponse(oAuth2User.getAttributes());
        }

        else if ("kakao".equals(registrationId)) {
            response = new KakaoResponse(oAuth2User.getAttributes());
        }

        else return null;

        // 로그인 완료 후 진행 로직
        String username = response.getProvider() + " " + response.getProviderId();
        Optional<User> optionalUser = userRepository.findByUsername(username);

        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            user.updateUserInfo(response.getEmail(), response.getName());
        } else {
            // 생일 처리
            LocalDate birth;
            try {
                birth = LocalDate.parse(response.getBirthYear() + response.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (Exception e) {
                birth = LocalDate.of(2000, 1, 1); // 기본값
            }

            // 성별 변환
            Gender gender;
            try {
                gender = Gender.valueOf(response.getGender().toUpperCase());
            } catch (Exception e) {
                gender = Gender.UNKNOWN;
            }

            // 유저 생성
            user = User.create(
                    response.getName(),                    // nickname
                    response.getEmail(),
                    birth,
                    gender,
                    Role.USER,
                    AuthProvider.valueOf(response.getProvider().toUpperCase()),
                    "default.png"
            );
        }

        userRepository.save(user);

        UserDto userDto = new UserDto(user);
        return new CustomOAuth2User(userDto);

    }
}
