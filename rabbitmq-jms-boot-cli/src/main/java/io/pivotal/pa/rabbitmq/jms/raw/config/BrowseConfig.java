package io.pivotal.pa.rabbitmq.jms.raw.config;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.pivotal.pa.rabbitmq.jms.raw.client.JMSClientWorker;
import io.pivotal.pa.rabbitmq.jms.raw.client.MessageBrowserClient;
import io.pivotal.pa.rabbitmq.jms.raw.properties.AMQPProperties;
import io.pivotal.pa.rabbitmq.jms.raw.properties.JMSProperties;

@Profile("browse")
@Configuration
public class BrowseConfig {
	private static Logger log = LoggerFactory.getLogger(SenderConfig.class);

	@Bean
	public Session queueSession(Connection connection) {
		Session session = null;
		int ackMode = 0;
		boolean transacted = false;
		
		//Create the session
		try {
			log.info("Creating queue session");
			session = connection.createSession(transacted, ackMode);
		} catch (JMSException e) {
			e.printStackTrace();
			session = null;
		}
		return session;
	}	
	@Bean
	public JMSClientWorker messageBrowserClient() {
		return new MessageBrowserClient();
	}
			
	@Profile("browse")
	@Bean
	public QueueBrowser queueBrowser(Session queueSession, AMQPProperties amqpProperties, JMSProperties jmsProperties) {
		QueueBrowser messageBrowser = null;
		try {
			if(amqpProperties.amqpExchangeName != null && !"".equals(amqpProperties.amqpExchangeName)) {
				log.info("Not a valid option for queue browser");
				messageBrowser = queueSession.createBrowser(queueSession.createQueue(jmsProperties.queueName));
			}
			else {
				log.info("Creating QueueBrowser using JMS Queue obj for queueName="+jmsProperties.queueName);
				messageBrowser = queueSession.createBrowser(queueSession.createQueue(jmsProperties.queueName));
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return messageBrowser;
	}

}
