package com.playus.userservice.domain.user.feign.fallback;

import com.playus.userservice.domain.user.feign.client.TwpFeignClient;
import com.playus.userservice.domain.user.feign.response.PartyApplicationInfoFeignResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Component
public class TwpFeignFallback implements TwpFeignClient {

    @Override
    public List<PartyApplicationInfoFeignResponse> getMyApplications(@RequestParam("userId") Long userId) {
        log.error("Feign fallback: getMyApplications 호출 실패. userId={}", userId);
        return List.of(
                PartyApplicationInfoFeignResponse.withServiceUnavailable()
        );
    }
}
