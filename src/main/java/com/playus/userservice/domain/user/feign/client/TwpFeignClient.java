package com.playus.userservice.domain.user.feign.client;

import com.playus.userservice.domain.user.feign.fallback.TwpFeignFallback;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;


import java.util.List;

@FeignClient(name = "twpFeignClient", url = "${feign.twp.url}", path = "/twp/api", fallback = TwpFeignFallback.class)
@CircuitBreaker(name = "circuit")
public interface TwpFeignClient {



}
