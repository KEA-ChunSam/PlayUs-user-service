package com.playus.userservice.domain.user.specification;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.review.UserTagSummaryResponse;
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

@Tag(name = "UserTag", description = "유저 태그 통계 조회 API")
public interface UserTagReadControllerSpecification {

    @Operation(
            summary     = "유저 태그 요약 조회",
            description = """
            인증된 사용자가 다른 유저의 태그 요약을 조회합니다.

            · `total`       : 해당 유저에 기록된 전체 태그 수  
            · `positiveTag` : tag_id=2(부정 태그)를 제외한 태그 수  
            · `topTags`     : tag_id=2를 제외한 빈도순 상위 3개의 태그 이름
            """,
            security    = @SecurityRequirement(name = "AccessCookie"),
            parameters = {
                    @Parameter(
                            name        = "Access",
                            description = "JWT access token (쿠키)",
                            in          = ParameterIn.COOKIE,
                            required    = true
                    ),
                    @Parameter(
                            name        = "user-id",
                            description = "조회 대상 유저 ID",
                            in          = ParameterIn.PATH,
                            required    = true,
                            example     = "7"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "total": 5,
                        "positiveTag": 3,
                        "topTags": [
                            "시간 약속을 잘 지켜요.",
                            "경기 직관이 열정적이에요.",
                            "상대방에 대한 배려심이 깊어요."
                            ]
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "사용자 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "code": 404,
                        "status": "NOT_FOUND",
                        "message": "사용자를 찾을 수 없습니다."
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
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
    ResponseEntity<UserTagSummaryResponse> getUserTagSummary(
            @Parameter(hidden = true) CustomOAuth2User principal,
            @Parameter(hidden = true) Long userId
    );
}
