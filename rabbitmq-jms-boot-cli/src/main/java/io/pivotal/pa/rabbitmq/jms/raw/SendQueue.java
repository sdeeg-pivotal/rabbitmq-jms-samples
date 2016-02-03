package io.pivotal.pa.rabbitmq.jms.raw;

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

@Profile({"send", "send-queue"})
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
	
	public void run() throws Exception {
		if(session != null) {
			Queue queue = session.createQueue(queueName);
			TextMessage message = session.createTextMessage();
			MessageProducer sender = session.createProducer(queue);
			System.out.println("Sending message \""+messageStr+"\" to queue "+queueName);
			try {
				for(int c=0; c<numMessages; c++) {
					message.setText("["+c+"] "+messageStr);
					sender.send(message);
					Thread.sleep(delay);
				}
			} catch(Exception ignore) {}
		} else {
			log.error("Session is null");
		}
	}

}
