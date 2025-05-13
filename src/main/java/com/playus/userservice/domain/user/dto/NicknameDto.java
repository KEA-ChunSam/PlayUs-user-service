package com.playus.userservice.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public class NicknameDto {

    private NicknameDto() {}

    // 요청: 새 닉네임
    public static class NicknameRequest {
        @NotBlank
        private final String nickname;

        public NicknameRequest(String nickname) {
            this.nickname = nickname;
        }
        public String getNickname() { return nickname; }

    }

    // 응답: 처리 결과
    public static class NicknameResponse {
        private final boolean success;
        private final String  message;

        public NicknameResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        public boolean isSuccess() { return success; }
        public String  getMessage() { return message; }

    }
}
