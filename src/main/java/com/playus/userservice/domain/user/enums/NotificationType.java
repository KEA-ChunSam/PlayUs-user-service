package com.playus.userservice.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    COMMENT("댓글"),
    PARTY_REQUEST("승인제 팟"),    // (받는 사람: 작성자)
    PARTY_JOINED("선착순 팟"),     // (받는 사람: 작성자)
    PARTY_APPROVED("신청 승인됨"),   // (받는 사람: 신청자)
    PARTY_REFUSED("신청 거절됨");   // (받는 사람: 신청자)
    private final String name;
}
