package com.playus.userservice.domain.user.feign.response;

import lombok.Builder;

@Builder
public record CommentInfo(
        Long id,          // 댓글 PK
        Long postId,      // 게시글 PK
        Long writerId,    // 댓글 작성자(userId)
        Long groupId,
        String content,
        boolean activated
) {
    public static CommentInfo of(
            Long id,
            Long postId,
            Long writerId,
            Long groupId,
            String content,
            boolean activated
    ) {
        return CommentInfo.builder()
                .id(id)
                .postId(postId)
                .writerId(writerId)
                .groupId(groupId)
                .content(content)
                .activated(activated)
                .build();
    }

    public static CommentInfo withServiceUnavailable() {
        return CommentInfo.builder()
                .id(null)
                .postId(null)
                .writerId(null)
                .groupId(null)
                .content("정보를 불러올 수 없습니다.")
                .activated(false)
                .build();
    }
}
