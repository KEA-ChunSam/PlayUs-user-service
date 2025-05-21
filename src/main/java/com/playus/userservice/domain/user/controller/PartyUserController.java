package com.playus.userservice.domain.user.controller;

import com.playus.userservice.domain.user.dto.partyuser.PartyApplicantsInfoFeignResponse;
import com.playus.userservice.domain.user.dto.partyuser.PartyUserThumbnailUrlListResponse;
import com.playus.userservice.domain.user.dto.partyuser.PartyWriterInfoFeignResponse;
import com.playus.userservice.domain.user.service.UserProfileReadService;
import com.playus.userservice.domain.user.specification.PartyUserControllerSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/api")
@RequiredArgsConstructor
public class PartyUserController implements PartyUserControllerSpecification {

    private final UserProfileReadService userProfileReadService;

    @PostMapping("/thumbnails")
    public PartyUserThumbnailUrlListResponse getPartyUserThumbnailUrls(
            @RequestBody List<Long> userIdList) {

        List<String> urls = userProfileReadService.fetchThumbnailUrls(userIdList);
        return new PartyUserThumbnailUrlListResponse(urls);
    }

    @PostMapping("/writers")
    public List<PartyWriterInfoFeignResponse> getWriterInfo(
            @RequestBody List<Long> writerIdList) {

        return userProfileReadService.fetchWriterInfos(writerIdList);
    }

    @PostMapping("/info")
    public List<PartyApplicantsInfoFeignResponse> getPartyApplicantsInfo(
            @RequestBody List<Long> userIdList) {

        return userProfileReadService.fetchApplicantsInfos(userIdList);
    }

}
