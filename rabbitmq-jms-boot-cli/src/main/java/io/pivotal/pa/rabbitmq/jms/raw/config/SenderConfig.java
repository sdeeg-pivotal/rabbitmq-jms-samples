package io.pivotal.pa.rabbitmq.jms.raw.config;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.rabbitmq.jms.admin.RMQDestination;

import io.pivotal.pa.rabbitmq.jms.raw.client.JMSClientWorker;
import io.pivotal.pa.rabbitmq.jms.raw.client.MessageSenderClient;

@Profile({ "send", "publish" })
@Configuration
public class SenderConfig {
	
	private static Logger log = LoggerFactory.getLogger(SenderConfig.class);

	@Bean
	public JMSClientWorker messageSenderClient() {
		return new MessageSenderClient();
	}

	@Profile("send")
	@Bean
	public MessageProducer queueMessageProducer(Session session, AMQPProperties amqpProperties, JMSProperties jmsProperties) {
		MessageProducer messageProducer = null;
		try {
			if(amqpProperties.amqpExchangeName != null && !"".equals(amqpProperties.amqpExchangeName)) {
				log.info("rmqExchangeName is set, using native RMQDestination to create MessageProducer.  queueName="+jmsProperties.queueName+", amqpExchangeName="+amqpProperties.amqpExchangeName);
				messageProducer = session.createProducer((Queue)(new RMQDestination(amqpProperties.amqpExchangeName, amqpProperties.amqpExchangeName, "", null)));
			}
			else {
				log.info("Creating MessageProducer using JMS Queue obj for queueName="+jmsProperties.queueName);
				messageProducer = session.createProducer(session.createQueue(jmsProperties.queueName));
			}
			if(!jmsProperties.persistent) {
				log.info("Setting delivery mode to NON_PERSISTENT");
				messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			} else { log.info("Setting delivery mode to PERSISTENT"); }
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return messageProducer;
	}

	@Profile("publish")
	@Bean
	public MessageProducer topicMessageProducer(Session session, AMQPProperties amqpProperties, JMSProperties jmsProperties) {
		MessageProducer messageProducer = null;
		try {
			if(amqpProperties.amqpExchangeName != null && !"".equals(amqpProperties.amqpExchangeName)) {
				log.info("rmqExchangeName is set, using native RMQDestination to create MessageProducer.  topicName="+jmsProperties.topicName+", amqpExchangeName="+amqpProperties.amqpExchangeName);
				messageProducer = session.createProducer((Topic)(new RMQDestination(amqpProperties.amqpExchangeName, amqpProperties.amqpExchangeName, null, null)));
			}
			else {
				log.info("Creating MessageProducer using JMS Queue obj for topicName="+jmsProperties.topicName);
				messageProducer = session.createProducer(session.createTopic(jmsProperties.topicName));
			}
			if(!jmsProperties.persistent) {
				log.info("Setting delivery mode to NON_PERSISTENT");
				messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			} else { log.info("Setting delivery mode to PERSISTENT"); }
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return messageProducer;
	}
	
	@Bean
	public TextMessage message(Session session) {
		try {
			return session.createTextMessage();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return null;
	}
}
