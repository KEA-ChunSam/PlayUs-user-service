package com.playus.userservice.domain.user.feign.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PartyApplicationInfoFeignResponse(
        List <String> partyAgeGroup,
        String partyGender,
        String title,
        String writer,
        String matchDate,
        String partyJoinRequestStatus
) {

    public static PartyApplicationInfoFeignResponse of(
            List <String> partyAgeGroup,
            String partyGender,
            String title,
            String writer,
            String matchDate,
            String partyJoinRequestStatus
    ) {
        return PartyApplicationInfoFeignResponse.builder()
                .partyAgeGroup(partyAgeGroup)
                .partyGender(partyGender)
                .title(title)
                .writer(writer)
                .matchDate(matchDate)
                .partyJoinRequestStatus(partyJoinRequestStatus)
                .build();
    }

    public static PartyApplicationInfoFeignResponse withServiceUnavailable() {
        return PartyApplicationInfoFeignResponse.builder()
                .partyAgeGroup(List.of())
                .partyGender("")
                .title("정보를 불러올 수 없습니다.")
                .writer("")
                .matchDate("")
                .partyJoinRequestStatus("UNKNOWN")
                .build();
    }
}
