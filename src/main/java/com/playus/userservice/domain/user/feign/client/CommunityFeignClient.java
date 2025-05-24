package com.playus.userservice.domain.user.feign.client;

import com.playus.userservice.domain.user.feign.fallback.CommunityFeignFallback;
import com.playus.userservice.domain.user.feign.response.CommentNotificationEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "communityFeignClient",
        url = "${feign.community.url}",
        path = "/community/api",
        fallback = CommunityFeignFallback.class)
@CircuitBreaker(name = "circuit")
public interface CommunityFeignClient {

    @GetMapping("/{id}")
    CommentNotificationEvent getComment(@PathVariable("id") Long id);

}
