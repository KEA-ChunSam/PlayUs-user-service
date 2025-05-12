package com.playus.userservice.domain.oauth.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class NaverResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> naverAccount;

    public NaverResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.naverAccount = (Map<String, Object>) attributes.get("naver_account");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return (String)((Map)attributes.get("response")).get("id");
    }

    @Override
    public String getPhoneNumber() {
        return (String)((Map)attributes.get("response")).get("mobile");
    }

    @Override
    public String getGender() {
        return (String) ((Map) attributes.get("response")).get("gender");
    }

    @Override
    public String getBirthday() {
        return (String) ((Map) attributes.get("response")).get("birthday");  // 예: "0824"
    }

    @Override
    public String getBirthYear() {
        return (String) ((Map) attributes.get("response")).get("birthyear");     // 예: "1999"
    }
}
