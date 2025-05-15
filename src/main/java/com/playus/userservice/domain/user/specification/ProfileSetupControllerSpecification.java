package com.playus.userservice.domain.user.specification;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.ProfileSetupDto.UserRegisterRequest;
import com.playus.userservice.domain.user.dto.ProfileSetupDto.UserRegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Profile-setup API Swagger specification.
 *
 * <p><b>인증 방식</b><br>
 *  로그인 시 발급된 <code>Access</code> 쿠키에 JWT가 담겨 있어야 합니다.<br>
 *  브라우저는 자동 전송, 비-브라우저 클라이언트(Postman·모바일 등)는
 *  <code>Cookie: Access&#x3D;&lt;JWT&gt;</code> 헤더를 직접 추가해야 합니다.</p>
 */

public interface ProfileSetupControllerSpecification {

    @Tag(name = "Post", description = "프로필 초기 설정 API")
    @Operation(
            summary = "프로필 초기 설정",
            description = "소셜 로그인 완료 후 처음 접근한 사용자가 선호팀과 닉네임을 등록합니다.",
            security    = @SecurityRequirement(name = "AccessCookie"),
            parameters  = @Parameter(
                    name        = "Access",
                    description = "JWT access token (쿠키)",
                    in          = ParameterIn.COOKIE,
                    required    = true,
                    example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            ),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "프로필 등록 요청 예시",
                                    value = """
                                            {
                                              "teamId": 7,
                                              "nickname": "플레이어"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "등록 성공",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "프로필 등록 응답 예시",
                                    value = """
                                        {
                                          "success": true,
                                          "message": "프로필이 정상적으로 설정되었습니다."
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "잘못된 요청 예시",
                                    value = """
                                        {
                                          "code": 400,
                                          "status": "BAD_REQUEST",
                                          "message": "nickname 필드는 필수입니다."
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "토큰 누락 또는 인증 실패",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "인증 실패 예시",
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
                    responseCode = "500", description = "서버 내부 오류",
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
    ResponseEntity<UserRegisterResponse> setupProfile(
            @Parameter(hidden = true)            // Swagger 문서에 노출하지 않음
            CustomOAuth2User principal,
            @Valid UserRegisterRequest request
    );
}
