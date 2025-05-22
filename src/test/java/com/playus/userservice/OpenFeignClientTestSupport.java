package com.playus.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;

@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "feign.twp.url=http://localhost:${wiremock.server.port}",
        "feign.community.url=http://localhost:${wiremock.server.port}",
        "feign.match.url=http://localhost:${wiremock.server.port}"
})
public abstract class OpenFeignClientTestSupport {

    @Autowired
    protected WireMockServer wireMockServer;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        wireMockServer.stop();
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
    }
}
