package io.pivotal.pa.rabbitmq.jms.raw;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"subscribe", "subscribe-topic"})
@Component
public class SubscribeTopic implements JMSTest {

	private static Logger log = LoggerFactory.getLogger(SubscribeTopic.class);

	@Autowired
	private Connection connection;
	
	@Autowired
	private Session session;

	@Value("${topic:default.topic.name}")
	String topicName;
	
	@Value("${durable:false}")
    boolean durable;

	@Override
	public void run() throws Exception {
		if (session != null) {
			Topic topic = session.createTopic(topicName);
			MessageConsumer consumer = null;
			if(durable) {
				consumer = session.createDurableSubscriber(topic, "abc123");
			} else {
				consumer = session.createConsumer(topic);
			}

			System.out.println("Registering subscriber for topic " + topicName + " (enter 'x' to exit)");
			consumer.setMessageListener(new SimpleMessageListener());

			connection.start();

			//Wait for the x
			int ch;
			while ((ch = System.in.read()) != -1) {
				if (ch != '\n' && ch != '\r') {
					if (ch == 'x') {
						break;
					}
				}
			}

			connection.stop();
		} else {
			log.error("connectionFactory is null");
		}
	}
}
