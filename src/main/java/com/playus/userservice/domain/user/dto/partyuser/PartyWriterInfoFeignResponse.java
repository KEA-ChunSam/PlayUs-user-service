package com.playus.userservice.domain.user.dto.partyuser;

import lombok.Builder;

@Builder
public record PartyWriterInfoFeignResponse(
        Long id,
        String writerName,
        String writerGender,
        String writerThumbnailUrl
) {

    public static PartyWriterInfoFeignResponse of (Long id, String writerName,
                                                   String writerGender, String writerThumbnailUrl) {
        return PartyWriterInfoFeignResponse.builder()
                .id(id)
                .writerName(writerName)
                .writerGender(writerGender)
                .writerThumbnailUrl(writerThumbnailUrl)
                .build();
    }

}
