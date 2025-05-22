package com.playus.userservice.domain.user.dto.partyuser;

import lombok.Builder;

@Builder
public record PartyApplicantsInfoFeignResponse(
        Long userId,
        String name,
        int age,
        String thumbnailUrl
) {

    public static PartyApplicantsInfoFeignResponse of(Long userId, String name, int age, String thumbnailUrl) {
        return PartyApplicantsInfoFeignResponse.builder()
                .userId(userId)
                .name(name)
                .age(age)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

}
