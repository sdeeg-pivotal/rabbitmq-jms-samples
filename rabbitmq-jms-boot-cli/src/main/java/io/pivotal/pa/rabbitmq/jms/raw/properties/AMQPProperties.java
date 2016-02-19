package io.pivotal.pa.rabbitmq.jms.raw.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AMQPProperties {

	@Value("${amqp.uri:default}")
	public String amqpUri;

	@Value("${amqp.host:localhost}")
	public String host;
	
	@Value("${amqp.username:default}")
	public String username;
	
	@Value("${amqp.password:default}")
	public String password;
	
	@Value("${amqp.port:5672}")
	public int port;
	
	@Value("${amqp.vhost:/}")
	public String vHost;
	
	@Value("${amqp.ssl}")
	public boolean amqpSSL;

	@Value("${amqp.queue}")
	public String amqpQueueName;

	@Value("${amqp.exchange}")
	public String amqpExchangeName;

}
