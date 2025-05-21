package com.playus.userservice;


import com.playus.userservice.domain.oauth.service.AuthService;
import com.playus.userservice.global.s3.S3Service;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationTestSupport extends OpenFeignClientTestSupport {

    private static final String MYSQL_VERSION = "mysql:8.0.32";
    private static final String REDIS_VERSION = "redis:7.0.12";
    private static final String MONGO_VERSION = "mongodb/mongodb-community-server:latest";

    private static final MySQLContainer<?> mySQL;
    private static final GenericContainer redis;
    private static final GenericContainer readMongo;

    private static final int REDIS_PORT = 6379;
    private static final int MONGO_PORT = 27017;

    @MockitoBean
    protected S3Service s3Service;

    static {
        mySQL = new MySQLContainer<>(MYSQL_VERSION)
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofSeconds(60))
                .withReuse(true);

        redis = new GenericContainer(DockerImageName.parse(REDIS_VERSION))
                .withExposedPorts(REDIS_PORT)
                .withReuse(true);

        readMongo = new GenericContainer(DockerImageName.parse(MONGO_VERSION))
                .withExposedPorts(MONGO_PORT)
                .withReuse(true);

        mySQL.start();
        redis.start();
        readMongo.start();
    }

    @DynamicPropertySource
    public static void dynamicConfiguration(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQL::getJdbcUrl);
        registry.add("spring.datasource.username", mySQL::getUsername);
        registry.add("spring.datasource.password", mySQL::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(redis.getMappedPort(REDIS_PORT)));

        registry.add("spring.data.mongodb.read.uri",
                () -> String.format("mongodb://%s:%d/read_db", readMongo.getHost(), readMongo.getMappedPort(MONGO_PORT)));
        registry.add("spring.data.mongodb.read.port", () -> String.valueOf(readMongo.getMappedPort(MONGO_PORT)));

        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
    }
}
