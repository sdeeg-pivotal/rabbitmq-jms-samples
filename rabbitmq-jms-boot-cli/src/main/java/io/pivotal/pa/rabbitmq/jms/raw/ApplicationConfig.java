package io.pivotal.pa.rabbitmq.jms.raw;

import javax.jms.ConnectionFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.jms.admin.RMQObjectFactory;

@Configuration
public class ApplicationConfig {
	
	@Bean
	public RMQObjectFactory buildFactory() {
		return new RMQObjectFactory();
	}
	
	@Bean
	public ConnectionFactory getConnectionFactory(RMQObjectFactory objFactory) {
		return null;
	}

}
