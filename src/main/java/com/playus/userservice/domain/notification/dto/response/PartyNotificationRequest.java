package com.playus.userservice.domain.notification.dto.response;

import com.playus.userservice.domain.user.enums.NotificationType;
import jakarta.validation.constraints.NotNull;

public record PartyNotificationRequest(
        @NotNull
        Long receiverId,   // 알림을 받을 유저
        @NotNull
        Long partyId,      // 직관팟 ID
        @NotNull
        Long actorId,      // 알림을 발생시킨 유저
        @NotNull
        NotificationType type,
        @NotNull
        String title,
        @NotNull
        String content
) {}