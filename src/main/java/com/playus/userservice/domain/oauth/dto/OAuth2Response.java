package com.playus.userservice.domain.oauth.dto;

public interface OAuth2Response {
    String getProvider();
    String getProviderId();
    String getPhoneNumber();
    String getGender();
    String getBirthday();     // MMDD
    String getBirthYear();    // YYYY
}
