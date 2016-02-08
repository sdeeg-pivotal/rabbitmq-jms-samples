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
import org.springframework.beans.factory.annotation.Value;
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
	public JMSClientWorker senderRunner() {
		return new MessageSenderClient();
	}

	@Value("${jms.queue:default.queue}")
	private String queueName;

	@Value("${amqp.exchange}")
	private String amqpExchangeName;

	@Value("${amqp.queue}")
	private String amqpQueueName;

	@Value("${amqp.routing-key}")
	private String amqpRoutingKey;

	@Value("${jms.persistent:false}")
	private boolean persistent;

	@Profile("send")
	@Bean
	public MessageProducer queueMessageProducer(Session session) {
		MessageProducer messageProducer = null;
		try {
			if(amqpExchangeName != null && !"".equals(amqpExchangeName)) {
				log.info("rmqExchangeName is set, using native RMQDestination to create MessageProducer.  queueName="+queueName+", amqpExchangeName="+amqpExchangeName);
				messageProducer = session.createProducer((Queue)(new RMQDestination(queueName, amqpExchangeName, "", null)));
			}
			else {
				log.info("Creating MessageProducer using JMS Queue obj for queueName="+queueName);
				messageProducer = session.createProducer(session.createQueue(queueName));
			}
			if(!persistent) {
				log.info("Setting delivery mode to NON_PERSISTENT");
				messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			} else { log.info("Setting delivery mode to PERSISTENT"); }
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return messageProducer;
	}

	@Value("${jms.topic:default.topic}")
	private String topicName;

	@Profile("publish")
	@Bean
	public MessageProducer topicMessageProducer(Session session) {
		MessageProducer messageProducer = null;
		try {
			if(amqpExchangeName != null && !"".equals(amqpExchangeName)) {
				log.info("rmqExchangeName is set, using native RMQDestination to create MessageProducer.  topicName="+topicName+", amqpExchangeName="+amqpExchangeName);
				messageProducer = session.createProducer((Topic)(new RMQDestination(topicName, amqpExchangeName, null, null)));
			}
			else {
				log.info("Creating MessageProducer using JMS Queue obj for topicName="+topicName);
				messageProducer = session.createProducer(session.createTopic(topicName));
			}
			if(!persistent) {
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
