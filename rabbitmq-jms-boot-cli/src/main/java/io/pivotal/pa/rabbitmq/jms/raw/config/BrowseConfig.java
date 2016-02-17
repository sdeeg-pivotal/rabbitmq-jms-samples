package io.pivotal.pa.rabbitmq.jms.raw.config;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import io.pivotal.pa.rabbitmq.jms.raw.client.JMSClientWorker;
import io.pivotal.pa.rabbitmq.jms.raw.client.MessageBrowserClient;
import io.pivotal.pa.rabbitmq.jms.raw.properties.AMQPProperties;
import io.pivotal.pa.rabbitmq.jms.raw.properties.JMSProperties;

@Profile("browse")

public class BrowseConfig {
	private static Logger log = LoggerFactory.getLogger(SenderConfig.class);
	
	@Bean
	public JMSClientWorker messageBrowserClient() {
		return new MessageBrowserClient();
	}
			
	@Profile("browse")
	@Bean
	public QueueBrowser queueBrowser(Session session, AMQPProperties amqpProperties, JMSProperties jmsProperties) {
		QueueBrowser messageBrowser = null;
		try {
			if(amqpProperties.amqpExchangeName != null && !"".equals(amqpProperties.amqpExchangeName)) {
				log.info("Not a valid option for queue browser");
			}
			else {
				log.info("Creating QueueBrowser using JMS Queue obj for queueName="+jmsProperties.queueName);
				messageBrowser = session.createBrowser(session.createQueue(jmsProperties.queueName));
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return messageBrowser;
	}

}
