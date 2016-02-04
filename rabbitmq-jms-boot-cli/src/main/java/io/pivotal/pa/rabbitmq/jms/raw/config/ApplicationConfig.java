package io.pivotal.pa.rabbitmq.jms.raw.config;

import java.util.Random;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!usage")
@Configuration
public class ApplicationConfig {

	@Bean
	public Random randy() { return new Random(System.currentTimeMillis()); }
}
