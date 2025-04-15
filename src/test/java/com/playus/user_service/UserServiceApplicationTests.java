package com.playus.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UserServiceApplication.class, properties = "spring.profiles.active=test")
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
