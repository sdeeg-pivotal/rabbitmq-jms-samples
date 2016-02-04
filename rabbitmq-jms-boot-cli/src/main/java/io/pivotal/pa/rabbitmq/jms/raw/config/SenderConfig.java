package io.pivotal.pa.rabbitmq.jms.raw.config;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.pivotal.pa.rabbitmq.jms.raw.tests.JMSTest;
import io.pivotal.pa.rabbitmq.jms.raw.tests.MessageSenderTest;

@Profile({ "send", "send-queue", "publish", "publish-topic" })
@Configuration
public class SenderConfig {

	@Bean
	public JMSTest senderRunner() {
		return new MessageSenderTest();
	}

	@Value("${queue:default.topic.name}")
	private String queueName;

	@Profile({ "send", "send-queue" })
	@Bean
	public MessageProducer queueMessageProducer(Session session) {
		try {
			return session.createProducer(session.createQueue(queueName));
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Value("${topic:default.topic.name}")
	private String topicName;

	@Profile({"publish", "publish-topic"})
	@Bean
	public MessageProducer topicMessageProducer(Session session) {
		try {
			return session.createProducer(session.createTopic(topicName));
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Bean
	public TextMessage message(Session session) {
		try {
			return session.createTextMessage();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
