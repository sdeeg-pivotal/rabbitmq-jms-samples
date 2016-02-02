package io.pivotal.rabbitmq.jms.boot.demo;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

@Configuration
public class SpringRabbitJMSConfig {

	private static final Log log = LogFactory.getLog(SpringRabbitJMSConfig.class);

	@Value("${amqp.uri:default}")
	String amqpURI;
	
	@Value("${amqp.host:default}")
	String host;
	
	@Value("${amqp.username:default}")
	String username;
	
	@Value("${amqp.password:default}")
	String password;
	
	@Value("${amqp.vhost:default}")
	String vhost;
	
	@Bean
	ConnectionFactory connectionFactory() {
		
		log.info("Connecting to RabbitMQ at URI:"+amqpURI);
		RMQConnectionFactory rmqcf = new RMQConnectionFactory();
		try {
			if(!"default".equals(amqpURI)) {
			rmqcf.setUri(amqpURI);
			}
			else {
				rmqcf.setHost(host);
				rmqcf.setUsername(username);
				rmqcf.setPassword(password);
				if(vhost == null || " ".equals(vhost)) { vhost="/"; }
				rmqcf.setVirtualHost(vhost);
				System.out.println("The URI: "+rmqcf.getUri());
			}
		} catch(Exception e) {
			rmqcf = null;
			log.error(e.toString());
		}
		return rmqcf;
	}

}
