package io.pivotal.pa.rabbitmq.jms.raw.config;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.rabbitmq.jms.admin.RMQDestination;

import io.pivotal.pa.rabbitmq.jms.raw.client.JMSClientWorker;
import io.pivotal.pa.rabbitmq.jms.raw.client.MessageSenderClient;
import io.pivotal.pa.rabbitmq.jms.raw.properties.AMQPProperties;
import io.pivotal.pa.rabbitmq.jms.raw.properties.JMSProperties;

@Profile({ "send", "publish" })
@Configuration
public class SenderConfig {
	
	private static Logger log = LoggerFactory.getLogger(SenderConfig.class);
	
	@Autowired
	private Session jmsSession;

	@Autowired
    private AMQPProperties amqpProperties;
	
	@Autowired
    private JMSProperties jmsProperties;

	@Bean
	public JMSClientWorker messageSenderClient() {
		return new MessageSenderClient();
	}
	
	@Profile("send")
	@Bean
	public Queue queue() throws Exception {
		Queue queue = null;
		if(amqpProperties.amqpExchangeName != null && !"".equals(amqpProperties.amqpExchangeName)) {
			String queueName = (amqpProperties.amqpQueueName != null && !"".equals(amqpProperties.amqpQueueName)) ? amqpProperties.amqpQueueName : jmsProperties.queueName;
			log.info("rmqExchangeName is set, using native RMQDestination to create MessageProducer.  queueName="+queueName+", amqpExchangeName="+amqpProperties.amqpExchangeName);
			queue = new RMQDestination(amqpProperties.amqpExchangeName, amqpProperties.amqpExchangeName, queueName, null);
		}
		else {
			log.info("Creating MessageProducer using JMS Queue obj for queueName="+jmsProperties.queueName);
			queue = jmsSession.createQueue(jmsProperties.queueName);
		}
		return queue;
	}

	@Profile("send")
	@Bean
	public MessageProducer queueMessageProducer(Queue queue) {
		MessageProducer messageProducer = null;
		try {
			messageProducer = jmsSession.createProducer(queue);
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
	public Topic topic() throws Exception {
		Topic topic = null;
		if(amqpProperties.amqpExchangeName != null && !"".equals(amqpProperties.amqpExchangeName)) {
			log.info("rmqExchangeName is set, using native RMQDestination to create Topic.  topicName="+jmsProperties.topicName+", amqpExchangeName="+amqpProperties.amqpExchangeName);
			topic = new RMQDestination(amqpProperties.amqpExchangeName, amqpProperties.amqpExchangeName, jmsProperties.topicName, null);
		}
		else {
			log.info("Creating JMSTopic obj with topicName="+jmsProperties.topicName);
			topic = jmsSession.createTopic(jmsProperties.topicName);
		}
		return topic;
	}

	@Profile("publish")
	@Bean
	public MessageProducer topicMessageProducer(Topic topic) {
		MessageProducer messageProducer = null;
		try {
			messageProducer = jmsSession.createProducer(topic);
			if(!jmsProperties.persistent) {
				log.info("Setting delivery mode to NON_PERSISTENT");
				messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			} else { log.info("Setting delivery mode to PERSISTENT"); }
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return messageProducer;
	}

}
