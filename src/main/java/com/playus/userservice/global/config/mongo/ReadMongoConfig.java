package com.playus.userservice.global.config.mongo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ConditionalOnProperty(name = "spring.data.mongodb.read.enabled", havingValue = "true", matchIfMissing = true)
@EnableMongoRepositories(
        basePackages = "com.playus.userservice.domain.user.repository.read",
        mongoTemplateRef = "readMongoTemplate"
)
public class ReadMongoConfig {

}
