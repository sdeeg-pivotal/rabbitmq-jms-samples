package io.pivotal.pa.rabbitmq.jms.raw;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"consume","consume-queue"})
@Component
public class ConsumeQueue implements JMSTest {

	private static Logger log = LoggerFactory.getLogger(ConsumeQueue.class);

	@Autowired
	Connection connection;
	
	@Autowired
	private Session session;

	@Value("${queue:default.queue.name}")
	String queueName;

	@Override
	public void run() throws Exception {
		if (session != null) {
			Queue queue = session.createQueue(queueName);
			MessageConsumer consumer = session.createConsumer(queue);

			System.out.println("Registering listener for queue " + queueName + " (enter 'x' to exit)");
			consumer.setMessageListener(new SimpleMessageListener());

			connection.start();

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
