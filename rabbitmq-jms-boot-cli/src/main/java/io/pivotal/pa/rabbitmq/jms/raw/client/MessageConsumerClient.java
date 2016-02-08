package io.pivotal.pa.rabbitmq.jms.raw.client;

import javax.jms.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//This bean is created by the ConsumerConfig class if an appropriate profile is set.
public class MessageConsumerClient implements JMSClientApp {

	private static Logger log = LoggerFactory.getLogger(MessageConsumerClient.class);

	@Autowired
	private Connection connection;

	@Override
	public void run() throws Exception {
		if (connection != null) {

			System.out.println("Starting connection (press 'p' to pause or 'x' to exit)");
			connection.start();

			//Grab the input and Exit, Pause, or Resume
			int ch;
			while ((ch = System.in.read()) != -1) {
				if (ch == 'x' || ch == 'X') {
					System.out.println("Exiting");
					break;
				}
				else if (ch == 'p' || ch == 'P') {
					System.out.println("Pausing ... (press 'r' to resume)");
					connection.stop();
				}
				else if (ch == 'r' || ch == 'R') {
					System.out.println("Resuming (press 'p' to pause or 'x' to exit)");
					connection.start();
				}
			}

			connection.stop();
		} else {
			log.error("connectionFactory is null");
		}
	}
}
