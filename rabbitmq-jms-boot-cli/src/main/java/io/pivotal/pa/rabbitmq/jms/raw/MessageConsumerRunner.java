package io.pivotal.pa.rabbitmq.jms.raw;

import javax.jms.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"consume","consume-queue", "subscribe", "subscribe-topic"})
@Component
public class MessageConsumerRunner implements JMSTest {

	private static Logger log = LoggerFactory.getLogger(MessageConsumerRunner.class);

	@Autowired
	private Connection connection;

	@Override
	public void run() throws Exception {
		if (connection != null) {

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
