package com.playus.userservice.domain.oauth.specification;

import com.playus.userservice.global.request.TokenValidationRequest;
import com.playus.userservice.global.response.TokenValidationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Auth", description = "OAuth 토큰 검증 API")
public interface TokenValidationControllerSpecification {

    @Operation(
            summary     = "토큰 블랙리스트 여부 확인",
            description = "내부 MSA 호출용으로 전달된 JWT 토큰의 블랙리스트 등록 여부를 조회합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content  = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples  = @ExampleObject(
                                    name  = "토큰 검증 요청 예시",
                                    value = """
                                    {
                                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                    }
                                    """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "블랙리스트 조회 성공",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name  = "블랙리스트 미등록 예시",
                                            value = """
                                            {
                                                "blacklisted": false
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name  = "블랙리스트 등록 예시",
                                            value = """
                                            {
                                                "blacklisted": true
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 요청 - 토큰이 null이거나 유효하지 않음",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name  = "유효하지 않은 토큰 예시",
                                    value = """
                                    {
                                        "code": 400,
                                        "status": "BAD_REQUEST",
                                        "message": "유효하지 않은 토큰입니다."
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
                                    name  = "서버 오류 예시",
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
    ResponseEntity<TokenValidationResponse> checkBlacklist(
            @Parameter(hidden = true) TokenValidationRequest request
    );
}
