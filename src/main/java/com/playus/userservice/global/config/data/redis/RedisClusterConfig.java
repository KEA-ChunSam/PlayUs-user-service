package com.playus.userservice.global.config.data.redis;

import io.lettuce.core.SocketOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@Profile({"dev", "prod"})
public class RedisClusterConfig {

    @Value("${spring.data.redis.cluster.nodes}")
    private String redisClusterNodes;

    @Value("${spring.data.redis.cluster.max-redirects:3}")  // 기본값 3
    private int maxRedirects;

    @Value("${spring.data.redis.timeout:3s}")  // 기본값 3초
    private Duration timeout;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisClusterConfiguration clusterConfig = createClusterConfiguration();
        LettuceClientConfiguration clientConfig = createClientConfiguration();

        return new LettuceConnectionFactory(clusterConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 직렬화 설정
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();

        return template;
    }

    private RedisClusterConfiguration createClusterConfiguration() {
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration();

        // 노드 파싱
        String[] nodes = redisClusterNodes.split(",");
        for (String node : nodes) {
            String[] hostPort = node.trim().split(":");
            if (hostPort.length == 2) {
                clusterConfig.clusterNode(hostPort[0].trim(), Integer.parseInt(hostPort[1].trim()));
            }
        }

        clusterConfig.setMaxRedirects(maxRedirects);

        return clusterConfig;
    }

    private LettuceClientConfiguration createClientConfiguration() {
        // K8s 환경 최적화된 소켓 옵션
        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofSeconds(5))          // K8s 네트워크 지연 고려
                .keepAlive(true)                                // Keep-alive 활성화
                .tcpNoDelay(true)                               // 지연 최소화
                .build();

        // 클러스터 토폴로지 새로고침 - K8s 환경용
        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh(Duration.ofSeconds(30))  // K8s Pod 재시작 빠른 감지
                .enableAllAdaptiveRefreshTriggers()             // 장애 시 빠른 감지
                .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(5))
                .build();

        // 클러스터 클라이언트 옵션
        ClusterClientOptions clientOptions = ClusterClientOptions.builder()
                .socketOptions(socketOptions)
                .topologyRefreshOptions(topologyRefreshOptions)
                .autoReconnect(true)                           // 자동 재연결
                .pingBeforeActivateConnection(true)            // 연결 전 상태 확인
                .validateClusterNodeMembership(true)           // 클러스터 검증 활성화
                .build();

        return LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .commandTimeout(timeout)
                .shutdownTimeout(Duration.ofSeconds(1))        // 빠른 셧다운
                .build();
    }
}
