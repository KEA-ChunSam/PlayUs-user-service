package com.playus.userservice.domain.user.feign.fallback;

import com.playus.userservice.domain.user.feign.client.MatchFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MatchFeignFallback implements MatchFeignClient {

    @Override
    public LocalDateTime getMatchDate(Long matchId) {
        log.error("503 happened in MatchFeignClient at fetching match date!!!");
        return LocalDateTime.of(2023, 10, 1, 0, 0);
    }
}
