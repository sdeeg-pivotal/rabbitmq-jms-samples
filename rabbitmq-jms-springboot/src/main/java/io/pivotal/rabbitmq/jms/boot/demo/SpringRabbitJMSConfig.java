package io.pivotal.rabbitmq.jms.boot.demo;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

@Configuration
public class SpringRabbitJMSConfig {
	@Value("${amqp.uri:amqp://localhost}")
	String amqpURI;
	
	@Bean
	ConnectionFactory connectionFactory() {
		RMQConnectionFactory rmqcf = new RMQConnectionFactory();
		try {
			rmqcf.setUri(amqpURI);
		} catch(Exception ignore) { rmqcf = null; }
		return rmqcf;
	}

}
