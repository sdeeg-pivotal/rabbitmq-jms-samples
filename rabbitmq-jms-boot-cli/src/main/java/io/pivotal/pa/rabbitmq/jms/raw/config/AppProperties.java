package io.pivotal.pa.rabbitmq.jms.raw.config;

import org.springframework.beans.factory.annotation.Value;

public class AppProperties {

	@Value("${amqp.uri:default}")
	private String amqpUri;

	@Value("${amqp.host:localhost}")
	private String host;
	
	@Value("${amqp.username:default}")
	private String username;
	
	@Value("${amqp.password:default}")
	private String password;
	
	@Value("${amqp.port:5672}")
	private int port;
	
	@Value("${amqp.vhost:/}")
	private String vHost;

}
