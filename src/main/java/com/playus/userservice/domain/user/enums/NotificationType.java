package com.playus.userservice.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    INVITATION("초대"),
    COMMENT("댓글");

    private final String name;
}
