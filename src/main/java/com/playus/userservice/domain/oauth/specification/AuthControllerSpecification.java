package com.playus.userservice.domain.oauth.specification;

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

/**
 * Access/Refresh 토큰 재발급·로그아웃 API Swagger 명세.
 *
 * <p><b>인증 방식</b><br>
 *   모든 요청은 <code>Access</code>(필수)·<code>Refresh</code>(선택) 쿠키를 포함해야 합니다.
 *  브라우저는 자동 전송, 비-브라우저(Postman·앱 등)는
 *  <code>Cookie: Access=&lt;...&gt;; Refresh=&lt;...&gt;</code> 헤더를 직접 추가해야 합니다.
 * </p>
 */
public interface AuthControllerSpecification {

    /* ---------- token re-issue ---------- */

    @Tag(name = "Post", description = "JWT 재발급 API")
    @Operation(
            summary     = "Access / Refresh 재발급",
            description = """
                          만료 여부에 따라  
                          • Access만 재발급  
                          • Refresh만 재발급  
                          • 두 토큰 모두 재발급  
                          을 수행합니다.
                          """,
            security    = @SecurityRequirement(name = "AccessCookie"),
            parameters  = {
                    @Parameter(
                            in          = ParameterIn.COOKIE,
                            name        = "Access",
                            description = "만료된(또는 유효한) JWT access token",
                            required    = true,
                            example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    ),
                    @Parameter(
                            in          = ParameterIn.COOKIE,
                            name        = "Refresh",
                            description = "만료-가능한 JWT refresh token",
                            required    = true,
                            example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    )
            }
    )
    @ApiResponses({
            /* 정상 – 본문 없음 */
            @ApiResponse(
                    responseCode = "200",
                    description  = "재발급 성공",
                    content      = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(name = "본문 없음", value = "")
                    )
            ),

            /* 실패 – 토큰 만료/변조 */
            @ApiResponse(
                    responseCode = "401",
                    description  = "토큰 만료/변조",
                    content      = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(
                                    name  = "만료·변조 예시",
                                    value = """
                                            {
                                              "code": 401,
                                              "status": "UNAUTHORIZED",
                                              "message": "토큰 만료/변조"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "서버 오류 예시",
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
    ResponseEntity<Void> reissue();

    /* ---------- logout ---------- */

    @Tag(name = "Post", description = "로그아웃 API")
    @Operation(
            summary   = "로그아웃",
            security  = @SecurityRequirement(name = "AccessCookie"),
            parameters = @Parameter(
                    in          = ParameterIn.COOKIE,
                    name        = "Access",
                    description = "로그아웃 대상 access token",
                    required    = true,
                    example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
    )
    @ApiResponses({
            /* 성공 – 204 No-Content */
            @ApiResponse(
                    responseCode = "204",
                    description  = "로그아웃 성공",
                    content      = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(name = "본문 없음", value = "")
                    )
            ),

            /* 실패 시(토큰 만료/블랙리스트) 예시 */
            @ApiResponse(
                    responseCode = "401",
                    description  = "유효하지 않은 토큰",
                    content      = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(
                                    name  = "만료·블랙리스트 예시",
                                    value = """
                                            {
                                              "code": 401,
                                              "status": "UNAUTHORIZED",
                                              "message": "MISSING_OR_INVALID_TOKEN"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "서버 오류 예시",
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
    ResponseEntity<Void> logout();
}
