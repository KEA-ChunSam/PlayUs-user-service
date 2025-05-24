package com.playus.userservice.domain.notification.specification;

import com.playus.userservice.domain.user.feign.response.CommentNotificationEvent;
import com.playus.userservice.domain.user.feign.response.PartyNotificationEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Notification-Internal", description = "커뮤니티/TWP 서비스 → 알림 API (내부 호출)")
public interface NotificationApiControllerSpecification {

    @Operation(
            summary     = "댓글 삭제 → 관련 알림 삭제",
            description = "커뮤니티 서비스에서 댓글 삭제 후 관련 알림을 정리합니다.",
            security    = @SecurityRequirement(name = "InternalServiceKey"),
            parameters  = @Parameter(
                    name        = "comment-id",
                    description = "삭제된 댓글 ID",
                    in          = ParameterIn.PATH,
                    required    = true,
                    example     = "42"
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 완료"),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE))
    })
    ResponseEntity<Void> deleteByCommentId(Long commentId);

    @Operation(
            summary     = "댓글 알림 생성",
            description = "커뮤니티 서비스에서 댓글 작성 시 알림을 생성합니다.",
            security    = @SecurityRequirement(name = "InternalServiceKey")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 완료"),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE))
    })
    ResponseEntity<Void> createCommentNotification(
            @Parameter(description = "댓글 알림 이벤트", required = true)
            CommentNotificationEvent event
    );

    @Operation(
            summary     = "직관팟 알림 생성",
            description = "TWP 서비스에서 직관팟 이벤트 발생 시 알림을 생성합니다.",
            security    = @SecurityRequirement(name = "InternalServiceKey")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 완료"),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE))
    })
    ResponseEntity<Void> createPartyNotification(
            @Parameter(description = "직관팟 알림 이벤트", required = true)
            PartyNotificationEvent event
    );
}
