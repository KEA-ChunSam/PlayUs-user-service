package com.playus.userservice.domain.user.feign.client;

import com.playus.userservice.domain.user.feign.fallback.MatchFeignFallback;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "matchFeignClient", url = "${feign.match.url}", path = "/match/api", fallback = MatchFeignFallback.class)
@CircuitBreaker(name = "circuit")
public interface MatchFeignClient {

    @GetMapping("/date")
    LocalDateTime getMatchDate(@RequestParam Long matchId);
}
