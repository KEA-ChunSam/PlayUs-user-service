package com.playus.userservice.domain.user.feign.response;

import lombok.Builder;

@Builder
public record CommentNotificationEvent(
        Long commentId,
        Long postId,
        Long writerId,
        String content,
        boolean activated
) {
    public static CommentNotificationEvent of(
            Long commentId,
            Long postId,
            Long writerId,
            String content,
            boolean activated
    ) {
        return CommentNotificationEvent.builder()
                .commentId(commentId)
                .postId(postId)
                .writerId(writerId)
                .content(content)
                .activated(activated)
                .build();
    }

    public static CommentNotificationEvent withServiceUnavailable() {
        return CommentNotificationEvent.builder()
                .commentId(null)
                .postId(null)
                .writerId(null)
                .content("정보를 불러올 수 없습니다.")
                .activated(false)
                .build();
    }
}
