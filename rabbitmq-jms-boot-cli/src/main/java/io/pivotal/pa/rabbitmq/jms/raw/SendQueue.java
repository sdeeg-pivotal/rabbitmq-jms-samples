package io.pivotal.pa.rabbitmq.jms.raw;

import java.time.LocalTime;

import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({ "send", "send-queue" })
@Component
public class SendQueue implements JMSTest {

	private static Logger log = LoggerFactory.getLogger(SendQueue.class);

	@Autowired
	private Session session;

	@Value("${queue:default.queue.name}")
	private String queueName;

	@Value("${message:default}")
	private String messageStr;

	@Value("${nummessages:0}")
	private int numMessages;

	@Value("${delay:100}")
	private long delay;

	@Value("${batchsize:0}")
	private int batchSize;

	public void run() throws Exception {
		int messageCounter = 0;

		if (numMessages < batchSize) {
			batchSize = numMessages;
		}
		if (session != null) {
			Queue queue = session.createQueue(queueName);
			TextMessage message = session.createTextMessage();
			MessageProducer sender = session.createProducer(queue);
			System.out.println("Sending " + numMessages + " messages with a delay of " + delay + " and payload \""
					+ messageStr + "\" to queue " + queueName);
			if (batchSize > 0) {
				System.out.println("Transactions are on.  Using a batch of " + batchSize);
			}
			try {
				for (messageCounter = 0; messageCounter < numMessages; messageCounter++) {
					message.setText("[" + messageCounter + "] " + messageStr);
					System.out.println(LocalTime.now()+"> Sending message [" + messageCounter + "]");
					sender.send(message);
					if (batchSize > 0) {
						if ((messageCounter + 1) % batchSize == 0) {
							System.out.println(LocalTime.now()+"> Committing transaction");
							session.commit();
						}
					}
					Thread.sleep(delay);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			log.error("Session is null");
		}
	}

}
