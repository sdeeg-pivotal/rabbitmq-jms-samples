package io.pivotal.pa.rabbitmq.jms.raw.config;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

@Profile("!usage")
@Configuration
public class JMSCommonConfig {
	
	private static Logger log = LoggerFactory.getLogger(JMSCommonConfig.class);

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
	
	@Bean
	public ConnectionFactory connectionFactory() {
		RMQConnectionFactory connectionFactory = new RMQConnectionFactory();
		try {
			if(amqpUri != null && !"default".equals(amqpUri)) {
				connectionFactory.setUri(amqpUri);
			}
			else {
				connectionFactory.setHost(host);
				connectionFactory.setUsername(username);
				connectionFactory.setPassword(password);
				connectionFactory.setPort(port);
				connectionFactory.setVirtualHost(vHost);
			}
			System.out.println("Creating connection with URI: "+connectionFactory.getUri());
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
	
	@Value("${jms.ack:1}")
	private String ackModeStr;
	
	@Value("${batchsize:0}")
	private int batchSize;
	
	@Bean
	public Session jmsSession(Connection connection) {
		Session session = null;
		int ackMode = 0;
		boolean transacted = false;
		
		if(batchSize>0) {
			log.info("batchSize is "+batchSize+", turning transactions on.");
			transacted = true;
			ackMode = Session.SESSION_TRANSACTED;
		}
		else if("AUTO_ACKNOWLEDGE".equals(ackModeStr) || "1".equals(ackModeStr)) { ackMode = Session.AUTO_ACKNOWLEDGE; ackModeStr = "AUTO_ACKNOWLEDGE"; }
		else if("CLIENT_ACKNOWLEDGE".equals(ackModeStr) || "2".equals(ackModeStr)) { ackMode = Session.CLIENT_ACKNOWLEDGE; ackModeStr = "CLIENT_ACKNOWLEDGE"; }
		else if("DUPS_OK_ACKNOWLEDGE".equals(ackModeStr) || "3".equals(ackModeStr)) { ackMode = Session.DUPS_OK_ACKNOWLEDGE; ackModeStr = "DUPS_OK_ACKNOWLEDGE"; }
		else if("SESSION_TRANSACTED".equals(ackModeStr) || "0".equals(ackModeStr)) {
			transacted = true;
			ackMode = Session.SESSION_TRANSACTED;
			ackModeStr = "SESSION_TRANSACTED";
			if(batchSize<1) {
				log.warn("ACK mode set to SESSION_TRANSACTED, but batchSize not set.  Setting it to 1.");
				batchSize = 1;
			}
		}
		else {
			log.error("ACK mode \""+ackModeStr+"\" not set to a known type or value.  Using AUTO_ACKNOWLEDGE.");
			ackMode = Session.AUTO_ACKNOWLEDGE;
			ackModeStr = "AUTO_ACKNOWLEDGE";
		}
		try {
			log.info("Creating session (transacted="+transacted+", ack="+ackModeStr+")");
			session = connection.createSession(transacted, ackMode);
		} catch (JMSException e) {
			e.printStackTrace();
			session = null;
		}
		return session;
	}
}
