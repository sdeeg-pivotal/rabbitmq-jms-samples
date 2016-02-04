package io.pivotal.pa.rabbitmq.jms.raw.config;

import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.pivotal.pa.rabbitmq.jms.raw.tests.JMSTest;
import io.pivotal.pa.rabbitmq.jms.raw.tests.MessageConsumerTest;
import io.pivotal.pa.rabbitmq.jms.raw.tests.SimpleMessageListener;

@Profile({"consume", "subscribe"})
@Configuration
public class ConsumerConfig {
	
	private static Logger log = LoggerFactory.getLogger(ConsumerConfig.class);

	@Bean
	public JMSTest consumerRunner() {
		return new MessageConsumerTest();
	}
	
	@Value("${jms.queue:default.queue}")
	private String queueName;
	
	@Profile("consume")
	@Bean
	public MessageConsumer messageConsumer(Session session) {
		MessageConsumer messageConsumer = null;
		try {
			Queue queue = session.createQueue(queueName);
			messageConsumer = session.createConsumer(queue);

			System.out.println("Registering listener for queue "+queueName);
			messageConsumer.setMessageListener(new SimpleMessageListener());
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Value("${jms.topic:default.topic}")
	String topicName;
	
	@Value("${jms.durable-queue:not-durable}")
    String durableQueue;
	
	@Profile("subscribe")
	@Bean
	public MessageConsumer topicMessageConsumer(Session session) {
		MessageConsumer messageConsumer = null;
		try {
			Topic topic = session.createTopic(topicName);
			if(!"not-durable".equals(durableQueue)) {
				log.info("Creating durable queue for subscriber with name "+durableQueue);
				messageConsumer = session.createDurableSubscriber(topic, durableQueue);
			} else {
				messageConsumer = session.createConsumer(topic);
			}

			System.out.println("Registering listener for topic " + topicName);
			messageConsumer.setMessageListener(new SimpleMessageListener());
		} catch(Exception e) {
			e.printStackTrace();
			messageConsumer = null;
		}
		return messageConsumer;
	}
}
