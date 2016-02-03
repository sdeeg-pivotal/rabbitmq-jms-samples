package io.pivotal.pa.rabbitmq.jms.raw;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

@Configuration
public class ApplicationConfig {
	private static Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

	@Value("${jms.host:localhost}")
	private String host;
	
	@Value("${jms.username:default}")
	private String username;
	
	@Value("${jms.password:default}")
	private String password;
	
	@Bean
	public ConnectionFactory getConnectionFactory() {
		RMQConnectionFactory connectionFactory = new RMQConnectionFactory();
		connectionFactory.setHost(host);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		System.out.println("Creating connection with URI: "+connectionFactory.getUri());
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
			log.info("batchSize is "+batchSize+", setting transactions on.");
			transacted = true;
			ackMode = Session.SESSION_TRANSACTED;
		}
		else if("AUTO_ACKNOWLEDGE".equals(ackModeStr) || "1".equals(ackModeStr)) { ackMode = Session.AUTO_ACKNOWLEDGE; }
		else if("CLIENT_ACKNOWLEDGE".equals(ackModeStr) || "2".equals(ackModeStr)) { ackMode = Session.CLIENT_ACKNOWLEDGE; }
		else if("DUPS_OK_ACKNOWLEDGE".equals(ackModeStr) || "3".equals(ackModeStr)) { ackMode = Session.DUPS_OK_ACKNOWLEDGE; }
		else if("SESSION_TRANSACTED".equals(ackModeStr) || "0".equals(ackModeStr)) {
			transacted = true;
			ackMode = Session.SESSION_TRANSACTED;
			if(batchSize<1) {
				log.warn("ACK mode set to SESSION_TRANSACTED, but batchSize not set.  Setting it to 1.");
				batchSize = 1;
			}
		}
		else {
			log.error("ACK mode \""+ackModeStr+"\" not set to a known type or value.  Using AUTO_ACKNOWLEDGE.");
		}
		try {
			session = connection.createSession(transacted, ackMode);
		} catch (JMSException e) {
			e.printStackTrace();
			session = null;
		}
		return session;
	}
}
