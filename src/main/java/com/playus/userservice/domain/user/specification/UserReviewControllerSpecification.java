package com.playus.userservice.domain.user.specification;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.UserReviewRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "UserReview", description = "유저 후기(평가) API")
public interface UserReviewControllerSpecification {

    @Operation(
            summary     = "여러 유저 평가 반영",
            description = """
                직관팟 종료 후 인증된 사용자가 다수의 유저에 대해 한 번에 평가를 제출합니다.
                `positive=true`  → 대상 유저의 점수를 +0.01 하고, 태그를 user_tag 에 저장  
                `positive=false` → 대상 유저의 점수를 –0.01 하고, 태그를 user_tag 에 저장 (tag_id=2) 
                """,
            security    = @SecurityRequirement(name = "AccessCookie"),
            parameters  = @Parameter(
                    name        = "Access",
                    description = "JWT access token (쿠키)",
                    in          = ParameterIn.COOKIE,
                    required    = true,
                    example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "정상 처리",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserReviewRequest.class)),
                            examples = @ExampleObject(
                                    value = """
                            [
                                {
                                    "userId": 2,
                                    "tagId": 3,
                                    "positive": true
                                },
                                {
                                    "userId": 2,
                                    "tagId": 4,
                                    "positive": false
                                }
                            ]
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                            {
                                "code": 400,
                                "status": "BAD_REQUEST",
                                "message": "요청 형식이 올바르지 않습니다."
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                            {
                                "code": 401,
                                "status": "UNAUTHORIZED",
                                "message": "유효하지 않은 토큰입니다."
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "대상 유저 또는 태그 없음",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                            {
                                "code": 404,
                                "status": "NOT_FOUND",
                                "message": "리뷰 대상 유저가 존재하지 않습니다."
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                            {
                                "code": 500,
                                "status": "INTERNAL_SERVER_ERROR",
                                "message": "서버 내부 오류가 발생했습니다. 관리자에게 문의해 주세요."
                            }
                            """
                            )
                    )
            )
    })
    ResponseEntity<List<UserReviewRequest>> createUserReviews(
            @Parameter(hidden = true) CustomOAuth2User principal,
            @Parameter(
                    description = "유저 평가 리스트",
                    required = true,
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserReviewRequest.class)),
                            examples = @ExampleObject(
                                    name  = "리뷰 요청 예시",
                                    value = """
                                    [
                                        {
                                            "userId": 2,
                                            "tagId": 3,
                                            "positive": true
                                        },
                                        {
                                            "userId": 2,
                                            "tagId": 4,
                                            "positive": false
                                        }
                                    ]
                                    """
                            )
                    )
            )
            List<UserReviewRequest> requests
    );
}