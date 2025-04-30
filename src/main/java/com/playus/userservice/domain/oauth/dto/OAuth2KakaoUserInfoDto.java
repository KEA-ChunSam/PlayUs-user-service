/*
package com.playus.userservice.domain.oauth.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class OAuth2KakaoUserInfoDto {

    private String id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @ToString
    public static class KakaoAccount {

        private String email;

        private String name;

        private String gender;

        private String birthday;   // MMDD 형식 (예: 0824)

        @JsonProperty("birthyear")
        private String birthYear;  // YYYY 형식 (예: 1999)

    }
}
*/
