package com.playus.userservice.domain.user.specification;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.FavoriteTeamDto;
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
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "FavoriteTeam", description = "사용자의 선호 팀 관련 API")
public interface FavoriteTeamControllerSpecification {

    @Operation(
            summary     = "선호 팀 등록/수정 (단일)",
            description = "사용자의 선호 팀 하나를 등록하거나 수정합니다.",
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
                                    name  = "선호 팀 등록 요청 예시",
                                    value = """
                        {
                          "teamId": 7,
                          "displayOrder": 1
                        }
                        """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "처리 성공",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"success\":true,\"message\":\"선호 팀이 성공적으로 저장되었습니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":400,\"status\":\"BAD_REQUEST\",\"message\":\"teamId 필드는 필수입니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":401,\"status\":\"UNAUTHORIZED\",\"message\":\"유효하지 않은 토큰입니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":500,\"status\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 내부 오류가 발생했습니다. 관리자에게 문의해 주세요.\"}"
                            )
                    )
            )
    })
    ResponseEntity<FavoriteTeamDto.FavoriteTeamResponse> saveOrUpdateOne(
            @Parameter(hidden = true) CustomOAuth2User principal,
            @Valid FavoriteTeamDto.FavoriteTeamRequest request
    );

    @Operation(
            summary     = "선호 팀 순서 일괄 변경",
            description = "사용자의 선호 팀 목록을 순서에 맞게 일괄 업데이트합니다.",
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
                                    name  = "선호 팀 일괄 변경 요청 예시",
                                    value = """
                        [
                          { "teamId": 7, "displayOrder": 1 },
                          { "teamId": 8, "displayOrder": 2 }
                        ]
                        """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "처리 성공",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"success\":true,\"message\":\"선호 팀이 성공적으로 업데이트되었습니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":400,\"status\":\"BAD_REQUEST\",\"message\":\"requests 필드는 필수입니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":401,\"status\":\"UNAUTHORIZED\",\"message\":\"유효하지 않은 토큰입니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "팀을 찾을 수 없음",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":404,\"status\":\"NOT_FOUND\",\"message\":\"존재하지 않는 팀입니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":500,\"status\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 내부 오류가 발생했습니다. 관리자에게 문의해 주세요.\"}"
                            )
                    )
            )
    })
    ResponseEntity<FavoriteTeamDto.FavoriteTeamResponse> updateMany(
            @Parameter(hidden = true) CustomOAuth2User principal,
            @Valid List<FavoriteTeamDto.FavoriteTeamRequest> requests
    );
}
