package com.playus.userservice.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public class NicknameDto {
    private NicknameDto() {}

    public static class NicknameRequest {
        @NotBlank(message = "nickname 필드는 필수입니다.")
        private String nickname;

        // 기본 생성자
        public NicknameRequest() {}

        // 편의 생성자(서비스/테스트에서 사용할 수 있게)
        public NicknameRequest(String nickname) {
            this.nickname = nickname;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }

    public static class NicknameResponse {
        private final boolean success;
        private final String message;

        public NicknameResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
