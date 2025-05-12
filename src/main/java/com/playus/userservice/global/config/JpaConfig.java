package com.playus.userservice.global.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.playus.userservice.domain.user.repository.write"
)
@EntityScan(basePackages = "com.playus.userservice.domain.user.entity")
public class JpaConfig {
}
