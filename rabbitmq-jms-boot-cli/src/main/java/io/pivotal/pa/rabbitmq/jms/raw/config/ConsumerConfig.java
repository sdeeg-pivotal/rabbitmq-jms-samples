package io.pivotal.pa.rabbitmq.jms.raw.config;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
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
import io.pivotal.pa.rabbitmq.jms.raw.client.MessageConsumerClient;
import io.pivotal.pa.rabbitmq.jms.raw.client.SimpleMessageListener;
import io.pivotal.pa.rabbitmq.jms.raw.client.SingleThreadedMessageListener;
import io.pivotal.pa.rabbitmq.jms.raw.properties.AMQPProperties;
import io.pivotal.pa.rabbitmq.jms.raw.properties.AppProperties;
import io.pivotal.pa.rabbitmq.jms.raw.properties.JMSProperties;

@Profile({"consume", "subscribe"})
@Configuration
public class ConsumerConfig {
	
	private static Logger log = LoggerFactory.getLogger(ConsumerConfig.class);
	
	@Autowired
	private Session jmsSession;

	@Autowired
    private AMQPProperties amqpProperties;
	
	@Autowired
    private JMSProperties jmsProperties;
	
	@Autowired
	private AppProperties appProperties;

	@Bean
	public JMSClientWorker messageConsumerClient() {
		return new MessageConsumerClient();
	}

	@Bean
	public SimpleMessageListener simpleMessageListener() {
		return new SimpleMessageListener();
	}
	
	@Bean
	public SingleThreadedMessageListener singleThreadedMessageListener(AppProperties appProperties) {
		SingleThreadedMessageListener singleThreadedMessageListener = null;
		if(appProperties.poisonTryLimit>0) {
			singleThreadedMessageListener = new SingleThreadedMessageListener();
		}
		return singleThreadedMessageListener;
	}

	@Profile("consume")
	@Bean
	public Queue queue() throws Exception {
		Queue queue = null;
		if(amqpProperties.amqpQueueName != null && !"".equals(amqpProperties.amqpQueueName)) {
			log.info("rmqExchangeName is set, using native RMQDestination to create Queue.  queueName="+amqpProperties.amqpQueueName);
			queue = new RMQDestination(amqpProperties.amqpQueueName, null, null, amqpProperties.amqpQueueName);
		}
		else {
			log.info("Creating MessageProducer using JMS Queue obj for queueName="+jmsProperties.queueName);
			queue = jmsSession.createQueue(jmsProperties.queueName);
		}
		return queue;
	}
	
	@Profile("consume")
	@Bean
	public MessageConsumer messageConsumer(SimpleMessageListener simpleMessageListener, Queue queue) {
		MessageConsumer messageConsumer = null;
		try {
			messageConsumer = jmsSession.createConsumer(queue);

			if(appProperties.poisonTryLimit <= 0) {
				log.info("Registering simpleMessaegListener for queue");
				messageConsumer.setMessageListener(simpleMessageListener);
			}
			else {
				log.info("We're listening for poison messages, so switching from a setMessageListener to single threaded mode.");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return messageConsumer;
	}
	
	@Profile("subscribe")
	@Bean
	public Topic topic() throws Exception {
		Topic topic = null;
		if(amqpProperties.amqpQueueName != null && !"".equals(amqpProperties.amqpQueueName)) {
			log.info("rmqQueueName is set, using native RMQDestination to create Topic for MessageConsumer.  queueName="+jmsProperties.queueName+", amqpQueueName="+amqpProperties.amqpQueueName);
			topic = (Topic)(new RMQDestination(amqpProperties.amqpQueueName, null, null, amqpProperties.amqpQueueName));
		}
		else {
			topic = jmsSession.createTopic(jmsProperties.topicName);
		}
		return topic;
	}

	@Profile("subscribe")
	@Bean
	public MessageConsumer topicMessageConsumer(SimpleMessageListener simpleMessageListener, Topic topic) {
		MessageConsumer messageConsumer = null;
		try {
			if(!"not-durable".equals(jmsProperties.durableQueue)) {
				log.info("Creating durable queue for subscriber with name "+jmsProperties.durableQueue);
				messageConsumer = jmsSession.createDurableSubscriber(topic, jmsProperties.durableQueue);
			} else {
				log.info("Creating transient queue for subscriber");
				messageConsumer = jmsSession.createConsumer(topic);
			}

			if(appProperties.poisonTryLimit <= 0) {
				log.info("Registering simpleMessaegListener for queue");
				messageConsumer.setMessageListener(simpleMessageListener);
			}
			else {
				log.info("We're listening for poison messages, so switching from a setMessageListener to single threaded mode.");
			}
		} catch(Exception e) {
			e.printStackTrace();
			messageConsumer = null;
		}
		return messageConsumer;
	}

	@Bean
	public Session jmsPoisonResponseSession(Connection connection) {
		Session session = null;
		try {
			log.info("Creating session for poison message return queues (transacted=false, Session.AUTO_ACKNOWLEDGE)");
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			e.printStackTrace();
			session = null;
		}
		return session;
	}

	@Bean
	public MessageProducer backoutQueueProducer(Session jmsPoisonResponseSession) {
		MessageProducer messageProducer = null;
		try {
			if(appProperties.poisonTryLimit>0) {
				log.info("Creating the backoutQueueProducer sending to queue="+appProperties.poisonBackoutQueue);
				messageProducer = jmsPoisonResponseSession.createProducer(jmsPoisonResponseSession.createQueue(appProperties.poisonBackoutQueue));
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return messageProducer;
	}
	
//	@Bean
//	public MessageProducer reQueueProducer() {
//
//		MessageProducer messageProducer = null;
//
//		String destName = amqpProperties.amqpExchangeName;		
//		if(destName == null) {
//			destName = jmsProperties.queueName;
//		}
//
//		try {
//			if(appProperties.poisonTryLimit>0) {
//				log.info("Creating the reQueueProducer sending to queue="+destName);
//				messageProducer = session.createProducer(session.createQueue(destName));
//			}
//		} catch (JMSException e) {
//			e.printStackTrace();
//		}
//		return messageProducer;
//	}
}
