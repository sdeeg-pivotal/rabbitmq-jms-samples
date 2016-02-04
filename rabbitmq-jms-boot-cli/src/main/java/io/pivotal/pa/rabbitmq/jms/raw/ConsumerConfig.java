package io.pivotal.pa.rabbitmq.jms.raw;

import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"consume","consume-queue", "subscribe", "subscribe-topic"})
@Configuration
public class ConsumerConfig {

	@Bean
	public JMSTestRunner consumerRunner() {
		return new MessageConsumerRunner();
	}
	
	@Value("${queue:default.topic.name}")
	private String queueName;
	
	@Profile({"consume","consume-queue"})
	@Bean
	public MessageConsumer messageConsumer(Session session) {
		MessageConsumer messageConsumer = null;
		try {
			Queue queue = session.createQueue(queueName);
			messageConsumer = session.createConsumer(queue);

			System.out.println("Registering listener for queue " + queueName + " (enter 'x' to exit)");
			messageConsumer.setMessageListener(new SimpleMessageListener());
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Value("${topic:default.topic.name}")
	String topicName;
	
	@Value("${durable:false}")
    boolean durable;
	
	@Profile({"subscribe", "subscribe-topic"})
	@Bean
	public MessageConsumer topicMessageConsumer(Session session) {
		MessageConsumer messageConsumer = null;
		try {
			Topic topic = session.createTopic(topicName);
			if(durable) {
				messageConsumer = session.createDurableSubscriber(topic, "abc123");
			} else {
				messageConsumer = session.createConsumer(topic);
			}

			System.out.println("Registering subscriber for topic " + topicName + " (enter 'x' to exit)");
			messageConsumer.setMessageListener(new SimpleMessageListener());
		} catch(Exception e) {
			e.printStackTrace();
			messageConsumer = null;
		}
		return messageConsumer;
	}
}
