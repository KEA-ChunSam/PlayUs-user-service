package com.playus.userservice.domain.user.feign.response;

import com.playus.userservice.domain.user.enums.NotificationType;
import lombok.Builder;

@Builder
public record PartyNotificationEvent(
        Long partyId,
        String partyTitle,
        Long receiverId,       // 알림 받을 유저
        Long actorId,          // 행동 주체(요청자·참여자·작성자 등)
        NotificationType type, // PARTY_REQUEST, PARTY_JOINED, PARTY_APPROVED, PARTY_REFUSED
        String requestStatus,  //ACCEPT / REFUSE
        String requireMessage    // 신청 시 남긴 한 줄 메시지
) {

    /** 승인제 팟: 누군가 신청 (→ 작성자에게) */
    public static PartyNotificationEvent request(
            Long partyId, String partyTitle, Long receiverId, Long applicantId, String requestStatus, String requireMsg) {

        return PartyNotificationEvent.builder()
                .partyId(partyId)
                .partyTitle(partyTitle)
                .receiverId(receiverId)
                .actorId(applicantId)
                .type(NotificationType.PARTY_REQUEST)
                .requestStatus(requestStatus)
                .requireMessage(requireMsg)
                .build();
    }

    /** 선착순 팟: 누군가 바로 참가 (→ 작성자에게) */
    public static PartyNotificationEvent joined(
            Long partyId, String partyTitle, Long receiverId, Long applicantId) {

        return PartyNotificationEvent.builder()
                .partyId(partyId)
                .partyTitle(partyTitle)
                .receiverId(receiverId)
                .actorId(applicantId)                    // 참가자
                .type(NotificationType.PARTY_JOINED)
                .build();
    }

    /** 승인 결과 (→ 신청자에게) */
    public static PartyNotificationEvent approveResult(
            Long partyId, String partyTitle, Long receiverId, Long writerId, boolean approved) {

        return PartyNotificationEvent.builder()
                .partyId(partyId)
                .partyTitle(partyTitle)
                .receiverId(receiverId)              // 신청자
                .actorId(writerId)         // 작성자
                .type(approved
                        ? NotificationType.PARTY_APPROVED
                        : NotificationType.PARTY_REFUSED)
                .requestStatus(approved ? "ACCEPT" : "REFUSE")
                .build();
    }
}
