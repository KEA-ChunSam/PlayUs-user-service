package com.playus.userservice.domain.user.feign.fallback;

import com.playus.userservice.domain.user.feign.client.CommunityFeignClient;
import com.playus.userservice.domain.user.feign.response.CommentInfo;
import org.springframework.stereotype.Component;

@Component
public class CommunityFeignFallback implements CommunityFeignClient {
    @Override
    public CommentInfo getComment(Long id) {
        return CommentInfo.withServiceUnavailable();
    }
}
