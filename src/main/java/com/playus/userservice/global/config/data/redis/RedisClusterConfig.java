package com.playus.userservice.global.config.data.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@Profile({"dev", "prod"})  // dev, prod 프로필에서만 사용
public class RedisClusterConfig {

    @Value("${REDIS_CLUSTER_NODES}")
    private String redisClusterNodes;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration();

        String[] nodes = redisClusterNodes.split(",");
        for (String node : nodes) {
            String[] hostPort = node.trim().split(":");
            clusterConfig.clusterNode(hostPort[0], Integer.parseInt(hostPort[1]));
        }

        clusterConfig.setMaxRedirects(3);

        return new LettuceConnectionFactory(clusterConfig);
    }
}
