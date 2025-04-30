package com.playus.userservice.domain.oauth.dto;

public interface OAuth2Response {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getName();         // 이름
    String getGender();
    String getBirthday();     // MMDD
    String getBirthYear();    // YYYY
}
