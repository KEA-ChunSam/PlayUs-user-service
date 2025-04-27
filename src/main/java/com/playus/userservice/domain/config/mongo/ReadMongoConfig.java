package com.playus.userservice.domain.config.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "com.playus.userservice.domain.user.repository.read",
        mongoTemplateRef = "readMongoTemplate"
)
public class ReadMongoConfig {
}
