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

    @Tag(name = "Auth", description = "JWT 재발급 API")
    @Operation(
            summary     = "Access 재발급 (Refresh 회전 포함)",
            description = """
                      • 필수: `Refresh` 쿠키 1개만 전송  
                      • Refresh 토큰이 유효하면 새 Access 토큰을 내려주고,  
                      • Refresh 토큰의 남은 유효기간이 24 시간 미만이면 Refresh도 갱신합니다.
                      """,
            security    = @SecurityRequirement(name = "RefreshCookie"),
            parameters  = @Parameter(
                    in          = ParameterIn.COOKIE,
                    name        = "Refresh",
                    description = "유효한 JWT Refresh Token",
                    required    = true,
                    example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description  = "재발급 성공 (본문 없음)",
                    content      = @Content(mediaType = APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "400",
                    description  = "Refresh 토큰 누락",
                    content      = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(
                                    name  = "MISSING_REFRESH_TOKEN",
                                    value = """
                            {
                              "code": 400,
                              "status": "BAD_REQUEST",
                              "message": "MISSING_REFRESH_TOKEN"
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description  = "Refresh 토큰 오류",
                    content      = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = {
                                    @ExampleObject(
                                            name  = "REFRESH_TOKEN_EXPIRED",
                                            value = """
                                {
                                  "code": 401,
                                  "status": "UNAUTHORIZED",
                                  "message": "REFRESH_TOKEN_EXPIRED"
                                }
                                """
                                    ),
                                    @ExampleObject(
                                            name  = "INVALID_REFRESH_SESSION",
                                            value = """
                                {
                                  "code": 401,
                                  "status": "UNAUTHORIZED",
                                  "message": "INVALID_REFRESH_SESSION"
                                }
                                """
                                    ),
                                    @ExampleObject(
                                            name  = "REFRESH_TOKEN_MISMATCH",
                                            value = """
                                {
                                  "code": 401,
                                  "status": "UNAUTHORIZED",
                                  "message": "REFRESH_TOKEN_MISMATCH"
                                }
                                """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description  = "서버 오류",
                    content      = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(
                                    name  = "SERVER_ERROR",
                                    value = """
                            {
                              "code": 500,
                              "status": "INTERNAL_SERVER_ERROR",
                              "message": "Unexpected server error"
                            }
                            """
                            )
                    )
            )
    })
    ResponseEntity<Void> reissue();


    @Tag(name = "Auth", description = "로그아웃 API")
    @Operation(
            summary    = "로그아웃 (Refresh 삭제 & Access 블랙리스트)",
            security   = @SecurityRequirement(name = "AccessCookie"),
            parameters = @Parameter(
                    in          = ParameterIn.COOKIE,
                    name        = "Access",
                    description = "대상 Access Token (만료돼도 무방)",
                    required    = true,
                    example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description  = "로그아웃 성공 (본문 없음)",
                    content      = @Content(mediaType = APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description  = "토큰 누락·변조·블랙리스트",
                    content      = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(
                                    name  = "MISSING_OR_INVALID_TOKEN",
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
                    description  = "서버 오류",
                    content      = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(
                                    name  = "SERVER_ERROR",
                                    value = """
                            {
                              "code": 500,
                              "status": "INTERNAL_SERVER_ERROR",
                              "message": "Unexpected server error"
                            }
                            """
                            )
                    )
            )
    })
    ResponseEntity<Void> logout();
}
