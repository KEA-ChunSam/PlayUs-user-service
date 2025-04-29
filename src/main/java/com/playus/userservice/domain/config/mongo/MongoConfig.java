package com.playus.userservice.domain.config.mongo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfig {

    @Primary
    @Bean(name = "readMongoDbFactory")
    public MongoDatabaseFactory readMongoDbFactory(@Value("${spring.data.mongodb.read.uri}") String uri) {
        return new SimpleMongoClientDatabaseFactory(uri);
    }

    @Primary
    @Bean(name = "readMongoTemplate")
    public MongoTemplate readMongoTemplate(@Qualifier("readMongoDbFactory") MongoDatabaseFactory factory) {
        return new MongoTemplate(factory);
    }

}

