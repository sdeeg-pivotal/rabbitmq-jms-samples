package io.pivotal.pa.rabbitmq.jms.raw.config;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

import io.pivotal.pa.rabbitmq.jms.raw.properties.AMQPProperties;
import io.pivotal.pa.rabbitmq.jms.raw.properties.AppProperties;
import io.pivotal.pa.rabbitmq.jms.raw.properties.JMSProperties;

@Profile("!usage")
@Configuration
public class JMSCommonConfig {
	
	private static Logger log = LoggerFactory.getLogger(JMSCommonConfig.class);

	@Autowired
	private AppProperties appProperties;
	
	@Autowired
	private AMQPProperties amqpProperties;

	@Autowired
	private JMSProperties jmsProperties;
	
	@Bean
	public ConnectionFactory connectionFactory() {
		RMQConnectionFactory connectionFactory = new RMQConnectionFactory();
		try {
			if(amqpProperties.amqpUri != null && !"default".equals(amqpProperties.amqpUri)) {
				connectionFactory.setUri(amqpProperties.amqpUri);
			}
			else {
				connectionFactory.setHost(amqpProperties.host);
				connectionFactory.setUsername(amqpProperties.username);
				connectionFactory.setPassword(amqpProperties.password);
				connectionFactory.setPort(amqpProperties.port);
				connectionFactory.setVirtualHost(amqpProperties.vHost);
				connectionFactory.setSsl(amqpProperties.amqpSSL);
			}
			log.info("Creating connection with URI: "+connectionFactory.getUri());
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return connectionFactory;
	}
	
	@Bean
	public Connection jmsConnection(ConnectionFactory connectionFactory) {
		Connection connection = null;
		try {
			connection = connectionFactory.createConnection();
		} catch (JMSException e) {
			e.printStackTrace();
			connection = null;
		}
		return connection;
	}
	
	@Bean
	public Session jmsSession(Connection connection) {
		Session session = null;
		int ackMode = 0;
		boolean transacted = false;
		
		ackMode = getAckMode();
		transacted = (ackMode == Session.SESSION_TRANSACTED);
		
		//Create the session
		try {
			log.info("Creating session (transacted="+transacted+", ack="+jmsProperties.ackModeStr+")");
			session = connection.createSession(transacted, ackMode);
		} catch (JMSException e) {
			e.printStackTrace();
			session = null;
		}
		return session;
	}
	
	private int getAckMode() {
		int ackMode = -1;
		
		//Apply rules for modes driven by certain properties
		if(appProperties.batchSize>0) {
			log.info("batchSize is "+appProperties.batchSize+", turning transactions on.");
			jmsProperties.ackModeStr = "SESSION_TRANSACTED";
		}
		else if(appProperties.poisonTryLimit>0) {
			log.info("Poison message consumption enabled, setting mode to SESSION_TRANSACTED");
			jmsProperties.ackModeStr = "SESSION_TRANSACTED";
		}
		
		if("AUTO_ACKNOWLEDGE".equals(jmsProperties.ackModeStr) || "1".equals(jmsProperties.ackModeStr)) { ackMode = Session.AUTO_ACKNOWLEDGE; jmsProperties.ackModeStr = "AUTO_ACKNOWLEDGE"; }
		else if("CLIENT_ACKNOWLEDGE".equals(jmsProperties.ackModeStr) || "2".equals(jmsProperties.ackModeStr)) { ackMode = Session.CLIENT_ACKNOWLEDGE; jmsProperties.ackModeStr = "CLIENT_ACKNOWLEDGE"; }
		else if("DUPS_OK_ACKNOWLEDGE".equals(jmsProperties.ackModeStr) || "3".equals(jmsProperties.ackModeStr)) { ackMode = Session.DUPS_OK_ACKNOWLEDGE; jmsProperties.ackModeStr = "DUPS_OK_ACKNOWLEDGE"; }
		else if("SESSION_TRANSACTED".equals(jmsProperties.ackModeStr) || "0".equals(jmsProperties.ackModeStr)) {
			ackMode = Session.SESSION_TRANSACTED;
			jmsProperties.ackModeStr = "SESSION_TRANSACTED";
			if(appProperties.batchSize<1) {
				log.warn("ACK mode set to SESSION_TRANSACTED, but batchSize not set.  Setting it to 1.");
				appProperties.batchSize = 1;
			}
		}
		else {
			log.error("ACK mode \""+jmsProperties.ackModeStr+"\" not set to a known type or value.  Using AUTO_ACKNOWLEDGE.");
			ackMode = Session.AUTO_ACKNOWLEDGE;
			jmsProperties.ackModeStr = "AUTO_ACKNOWLEDGE";
		}
		return ackMode;
	}
}
