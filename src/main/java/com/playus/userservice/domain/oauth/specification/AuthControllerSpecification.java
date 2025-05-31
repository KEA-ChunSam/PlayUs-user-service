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

public interface AuthControllerSpecification {

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
            @ApiResponse(
                    responseCode = "200",
                    description  = "재발급 성공",
                    content      = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(name = "본문 없음", value = "")
                    )
            ),

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
            @ApiResponse(
                    responseCode = "204",
                    description  = "로그아웃 성공",
                    content      = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(name = "본문 없음", value = "")
                    )
            ),

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
