package com.playus.userservice.global.config.sentry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;

@Profile({"prod", "dev"})
@Configuration
public class SentryConfig {

	@Value("${sentry.dsn}")
	private String sentryDsn;

	@Value("${sentry.environment}")
	private String environment;

	@Value("${sentry.servername}")
	private String serverName;

	@PostConstruct
	public void initSentry() {
		Sentry.init(options -> {
			options.setDsn(sentryDsn);
			options.setEnvironment(environment);
			options.setServerName(serverName);
		});
	}
}
