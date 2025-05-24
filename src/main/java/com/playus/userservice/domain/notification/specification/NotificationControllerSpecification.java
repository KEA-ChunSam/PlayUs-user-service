package com.playus.userservice.domain.notification.specification;

import com.playus.userservice.domain.notification.dto.response.NotificationResponse;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@Tag(name = "Notification", description = "실시간 알림 스트림 및 조회 API")
public interface NotificationControllerSpecification {

    @Operation(
            summary     = "SSE 구독",
            description = "Server-Sent Events 스트림을 시작하여 실시간 알림을 수신합니다.",
            security    = @SecurityRequirement(name = "AccessCookie"),
            parameters  = {
                    @Parameter(
                            name        = "Access",
                            description = "JWT access token (쿠키)",
                            in          = ParameterIn.COOKIE,
                            required    = true,
                            example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    ),
                    @Parameter(
                            name        = "Last-Event-ID",
                            description = "클라이언트가 저장한 마지막 이벤트 ID (재연결 시)",
                            in          = ParameterIn.HEADER,
                            required    = false,
                            example     = "1_1715151515151"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "연결 성공",
                    content = @Content(mediaType = TEXT_EVENT_STREAM_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(value = """
                            {
                                "code": 401,
                                "status": "UNAUTHORIZED",
                                "message": "유효하지 않은 토큰입니다."
                            }""")
                    )
            )
    })
    ResponseEntity<SseEmitter> subscribe(
            @Parameter(hidden = true) CustomOAuth2User principal,
            String lastEventId
    );

    @Operation(
            summary     = "알림 읽음 처리",
            description = "특정 알림을 읽음 상태로 변경합니다.",
            security    = @SecurityRequirement(name = "AccessCookie"),
            parameters  = {
                    @Parameter(
                            name        = "Access",
                            description = "JWT access token (쿠키)",
                            in          = ParameterIn.COOKIE,
                            required    = true,
                            example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    ),
                    @Parameter(
                            name        = "notification-id",
                            description = "알림 ID",
                            in          = ParameterIn.PATH,
                            required    = true,
                            example     = "15"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(
                    responseCode = "400", description = "본인 알림 아님",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404", description = "알림 없음",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE)
            )
    })
    ResponseEntity<Void> readNotification(
            @Parameter(hidden = true) CustomOAuth2User principal,
            Long notificationId
    );

    @Operation(
            summary     = "전체 알림 목록",
            description = "사용자에게 발송된 모든 알림을 조회합니다.",
            security    = @SecurityRequirement(name = "AccessCookie"),
            parameters  = @Parameter(
                    name        = "Access",
                    description = "JWT access token (쿠키)",
                    in          = ParameterIn.COOKIE,
                    required    = true,
                    example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
    )
    @ApiResponse(
            responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE)
    )
    ResponseEntity<List<NotificationResponse>> getNotifications(
            @Parameter(hidden = true) CustomOAuth2User principal
    );

    @Operation(
            summary     = "최근 알림 3건",
            description = "가장 최근 3개의 알림을 조회합니다.",
            security    = @SecurityRequirement(name = "AccessCookie"),
            parameters  = @Parameter(
                    name        = "Access",
                    description = "JWT access token (쿠키)",
                    in          = ParameterIn.COOKIE,
                    required    = true,
                    example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
    )
    @ApiResponse(
            responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE)
    )
    ResponseEntity<List<NotificationResponse>> getRecentNotifications(
            @Parameter(hidden = true) CustomOAuth2User principal
    );
}
