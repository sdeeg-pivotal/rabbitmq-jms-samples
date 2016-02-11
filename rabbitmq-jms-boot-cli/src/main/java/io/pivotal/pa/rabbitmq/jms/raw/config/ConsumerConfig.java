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

import com.rabbitmq.jms.admin.RMQDestination;

import io.pivotal.pa.rabbitmq.jms.raw.client.JMSClientWorker;
import io.pivotal.pa.rabbitmq.jms.raw.client.MessageConsumerClient;
import io.pivotal.pa.rabbitmq.jms.raw.client.SimpleMessageListener;

@Profile({"consume", "subscribe"})
@Configuration
public class ConsumerConfig {
	
	private static Logger log = LoggerFactory.getLogger(ConsumerConfig.class);

	@Bean
	public JMSClientWorker messageConsumerClient() {
		return new MessageConsumerClient();
	}

	@Bean
	public SimpleMessageListener simpleMessageListener() {
		return new SimpleMessageListener();
	}
	
	@Value("${jms.queue:default.queue}")
	private String queueName;
	
	@Value("${amqp.queue}")
	private String amqpQueueName;
	
	@Profile("consume")
	@Bean
	public MessageConsumer messageConsumer(Session session, SimpleMessageListener simpleMessageListener) {
		MessageConsumer messageConsumer = null;
		try {
			if(amqpQueueName != null && !"".equals(amqpQueueName)) {
				log.info("rmqQueueName is set, using native RMQDestination to create MessageConsumer.  queueName="+queueName+", amqpQueueName="+amqpQueueName);
				messageConsumer = session.createConsumer((Queue)(new RMQDestination(queueName, null, null, amqpQueueName)));
			}
			else {
				messageConsumer = session.createConsumer(session.createQueue(queueName));
			}

			log.info("Registering listener for queue "+queueName);
			messageConsumer.setMessageListener(simpleMessageListener);
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
	public MessageConsumer topicMessageConsumer(Session session, SimpleMessageListener simpleMessageListener) {
		MessageConsumer messageConsumer = null;
		try {
			Topic topic = null;
			if(amqpQueueName != null && !"".equals(amqpQueueName)) {
				log.info("rmqQueueName is set, using native RMQDestination to create Topic for MessageConsumer.  queueName="+queueName+", amqpQueueName="+amqpQueueName);
				topic = (Topic)(new RMQDestination(topicName, null, null, amqpQueueName));
			}
			else {
				topic = session.createTopic(topicName);
			}
			
			if(!"not-durable".equals(durableQueue)) {
				log.info("Creating durable queue for subscriber with name "+durableQueue);
				messageConsumer = session.createDurableSubscriber(topic, durableQueue);
			} else {
				log.info("Creating transient queue for subscriber");
				messageConsumer = session.createConsumer(topic);
			}

			System.out.println("Registering listener for topic " + topicName);
			messageConsumer.setMessageListener(simpleMessageListener);
		} catch(Exception e) {
			e.printStackTrace();
			messageConsumer = null;
		}
		return messageConsumer;
	}

	
//	@Value("${poison.enabled:false}")
//	private boolean poisonEnabled;
//	public MessageProducer 
}
