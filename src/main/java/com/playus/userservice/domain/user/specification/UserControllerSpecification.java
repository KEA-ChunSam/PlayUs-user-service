package com.playus.userservice.domain.user.specification;


import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.withdraw.UserWithdrawResponse;
import com.playus.userservice.domain.user.dto.nickname.ProfileUpdateRequest;
import com.playus.userservice.domain.user.dto.nickname.NicknameResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "User", description = "사용자 관련 API")
public interface UserControllerSpecification {

    @Operation(
            summary     = "프로필 변경",
            description = "인증된 사용자의 프로필을 변경합니다.",
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
                    responseCode = "200", description = "변경 성공",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                            "success": true,
                            "message": "프로필이 성공적으로 변경되었습니다."
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
                    responseCode = "404", description = "사용자 없음",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
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
                    responseCode = "409", description = "닉네임 중복",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                            "code": 409,
                            "status": "CONFLICT",
                            "message": "이미 사용 중인 닉네임입니다."
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
    ResponseEntity<NicknameResponse> updateProfile(
            @Parameter(hidden = true) CustomOAuth2User principal,
            @Parameter(description = "새 닉네임", required = true) ProfileUpdateRequest request
    );

    @Operation(
            summary     = "회원 탈퇴",
            description = "인증된 사용자의 계정을 소프트 삭제(탈퇴) 처리합니다.",
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
                    responseCode = "200", description = "탈퇴 성공",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "회원 탈퇴가 완료되었습니다."
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
                    responseCode = "404", description = "사용자 없음",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                    {
                        "code": 404,
                        "status": "NOT_FOUND",
                        "message": "USER_NOT_FOUND"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409", description = "이미 탈퇴된 사용자",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                    {
                        "code": 409,
                        "status": "CONFLICT",
                        "message": "ALREADY_WITHDRAWN"
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
    ResponseEntity<UserWithdrawResponse> withdraw(
            @Parameter(hidden = true) CustomOAuth2User principal,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );
}
