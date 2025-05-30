package com.playus.userservice.global.config.data.mongodb;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Collections;

@Configuration
@ConditionalOnProperty(name = "spring.data.mongodb.read.enabled", havingValue = "true", matchIfMissing = true)
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

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Collections.emptyList());
    }

    @Bean
    public MongoMappingContext mongoMappingContext(MongoCustomConversions conversions) {
        MongoMappingContext context = new MongoMappingContext();
        context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        return context;
    }
}
