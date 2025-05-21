package com.playus.userservice.domain.user.feign.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PartyApplicationInfoFeignResponse(
        List<String> filters,
        String title,
        String writer,
        LocalDateTime date,
        String status,
        Long chatRoomId
) {

    public static PartyApplicationInfoFeignResponse of(
            List<String> filters,
            String title,
            String writer,
            LocalDateTime date,
            String status,
            Long chatRoomId
    ) {
        return PartyApplicationInfoFeignResponse.builder()
                .filters(filters)
                .title(title)
                .writer(writer)
                .date(date)
                .status(status)
                .chatRoomId(chatRoomId)
                .build();
    }

    public static PartyApplicationInfoFeignResponse withServiceUnavailable() {
        return PartyApplicationInfoFeignResponse.builder()
                .filters(List.of())
                .title("정보를 불러올 수 없습니다.")
                .writer("")
                .date(null)
                .status("UNKNOWN")
                .chatRoomId(null)
                .build();
    }
}