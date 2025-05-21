package com.playus.userservice.domain.user.specification;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.profile.UserProfileResponse;
import com.playus.userservice.domain.user.dto.profile.UserPublicProfileResponse;
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

@Tag(name = "UserProfile", description = "사용자 프로필 조회 API")
public interface UserProfileControllerSpecification {

    @Operation(
            summary     = "내 프로필 조회",
            description = "로그인한 사용자의 기본 정보와 선호 팀 목록을 조회합니다.",
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
                    responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name  = "프로필 조회 응답 예시",
                                    value = """
                    {
                        "id": 18,
                        "nickname": "default_nickname",
                        "phoneNumber": "+821079070479",
                        "birth": "2000-04-26",
                        "gender": "MALE",
                        "role": "USER",
                        "authProvider": "NAVER",
                        "activated": true,
                        "thumbnailURL": "default.png",
                        "userScore": 0.3,
                        "blockOff": null,
                        "favoriteTeams": [
                            { "teamId": 8, "displayOrder": 1 },
                            { "teamId": 1, "displayOrder": 2 }
                        ]
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
                    responseCode = "404", description = "사용자를 찾을 수 없음",
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
    ResponseEntity<UserProfileResponse> getProfile(
            @Parameter(hidden = true) CustomOAuth2User principal
    );

    @Operation(
            summary     = "다른 사람 프로필 조회",
            description = "로그인한 사용자가 다른 사용자의 공개 프로필을 조회합니다.",
            security    = @SecurityRequirement(name = "AccessCookie"),
            parameters = {
                    @Parameter(
                            name        = "Access",
                            description = "JWT access token (쿠키)",
                            in          = ParameterIn.COOKIE,
                            required    = true,
                            example     = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    ),
                    @Parameter(
                            name        = "targetUserId",
                            description = "조회할 사용자 ID",
                            in          = ParameterIn.PATH,
                            required    = true,
                            example     = "99"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name  = "다른 사람 프로필 조회 응답 예시",
                                    value = """
                    {
                      "id": 99,
                      "nickname": "other_user",
                      "gender": "FEMALE",
                      "thumbnailURL": "other.png",
                      "userScore": 2.5,
                      "favoriteTeams": [
                        { "teamId": 3, "displayOrder": 1 }
                      ]
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
                    responseCode = "404", description = "사용자를 찾을 수 없음",
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
    ResponseEntity<UserPublicProfileResponse> getOtherProfile(
            @Parameter(hidden = true) CustomOAuth2User principal,
            @Parameter(name = "targetUserId", hidden = true) Long targetUserId
    );
}
