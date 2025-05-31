package com.playus.userservice.domain.user.specification;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.presigned.PresignedUrlForSaveImageRequest;
import com.playus.userservice.domain.user.dto.presigned.PresignedUrlForSaveImageResponse;
import com.playus.userservice.domain.user.dto.profilesetup.UserRegisterRequest;
import com.playus.userservice.domain.user.dto.profilesetup.UserRegisterResponse;
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
                                            "message": "입력값에 대해 검증이 실패했습니다."
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

    @Operation(
            summary = "이미지 업로드용 Presigned URL 생성",
            description = "클라이언트가 S3에 이미지를 업로드할 수 있도록 Presigned URL을 반환합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Presigned URL 요청 예시",
                                    value = """
                                        {
                                            "fileName": "avatar.png",
                                            "contentType": "image/png"
                                        }
                                        """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "URL 생성 성공",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Presigned URL 응답 예시",
                                    value = """
                                        {
                                            "url": "https://bucket.s3.amazonaws.com/avatar.png?X-Amz-Algorithm=...",
                                            "key": "avatars/avatar.png"
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
                                            "message": "fileName 필드는 필수입니다."
                                        }
                                        """
                            )
                    )
            )
    })
    PresignedUrlForSaveImageResponse generatePresignedUrlForSaveImage(
            @Valid PresignedUrlForSaveImageRequest request
    );

}
