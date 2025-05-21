package com.playus.userservice.domain.user.specification;

import com.playus.userservice.domain.user.dto.partyuser.PartyUserThumbnailUrlListResponse;
import com.playus.userservice.domain.user.dto.partyuser.PartyWriterInfoFeignResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "PartyUser", description = "직관팟 사용자 관련 API")
public interface PartyUserControllerSpecification {

    @Operation(
            summary     = "직관팟 참여자 썸네일 조회",
            description = "직관팟 참여자들의 사용자 ID 목록을 받아, 각 사용자의 썸네일 URL 리스트를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name  = "썸네일 URL 리스트 응답 예시",
                                    value = """
                                    {
                                      "thumbnailUrls": [
                                        "https://example.com/avatar1.png",
                                          ]
                                    "https://example.com/avatar2.png"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (빈 ID 리스트 등)",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "code": 400,
                                      "status": "BAD_REQUEST",
                                      "message": "userIdList는 필수입니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
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
    PartyUserThumbnailUrlListResponse getPartyUserThumbnailUrls(
            @Parameter(
                    name        = "userIdList",
                    description = "썸네일을 조회할 사용자 ID 목록",
                    required    = true
            )
            List<Long> userIdList
    );

    @Operation(
            summary     = "직관팟 작성자 정보 조회",
            description = "직관팟 작성자의 ID 목록을 받아, 각 작성자의 id, nickname, gender, thumbnailURL을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name  = "작성자 정보 리스트 응답 예시",
                                    value = """
                                    [
                                      {
                                        "id": 31,
                                        "nickname": "writer1",
                                        "gender": "MALE",
                                        "thumbnailURL": "https://example.com/w1.png"
                                      },
                                      {
                                        "id": 32,
                                        "nickname": "writer2",
                                        "gender": "FEMALE",
                                        "thumbnailURL": "https://example.com/w2.png"
                                      }
                                    ]
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "작성자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    List<PartyWriterInfoFeignResponse> getWriterInfo(
            @Parameter(
                    name        = "writerIdList",
                    description = "작성자 정보를 조회할 사용자 ID 목록",
                    required    = true
            )
            List<Long> writerIdList
    );
}
