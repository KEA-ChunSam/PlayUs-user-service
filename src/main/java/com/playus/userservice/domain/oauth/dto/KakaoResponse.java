package com.playus.userservice.domain.oauth.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;

    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        return String.valueOf(kakaoAccount.get("email"));
    }

    @Override
    public String getName() {
        return String.valueOf(kakaoAccount.get("name"));
    }

    @Override
    public String getGender() {
        return String.valueOf(kakaoAccount.get("gender"));
    }

    @Override
    public String getBirthday() {
        return String.valueOf(kakaoAccount.get("birthday"));  // 예: "0824"
    }

    @Override
    public String getBirthYear() {
        return String.valueOf(kakaoAccount.get("birthyear")); // 예: "1999"
    }
}
