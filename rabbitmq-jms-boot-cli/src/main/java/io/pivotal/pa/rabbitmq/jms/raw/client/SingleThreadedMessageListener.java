package io.pivotal.pa.rabbitmq.jms.raw.client;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SingleThreadedMessageListener implements Runnable {

	private static Logger log = LoggerFactory.getLogger(SingleThreadedMessageListener.class);

	@Autowired
	private MessageConsumer messageConsumer;
	
	@Autowired
	private SimpleMessageListener simpleMessageListener;
	
	private boolean keepListening = true;
	
	@Override
	public void run() {

		if(messageConsumer != null) {
			log.info("Starting single threaded message listener loop ...");
			keepListening = true;
			try {
				Message message = messageConsumer.receive();
				while(keepListening) {
					if(message != null) { simpleMessageListener.onMessage(message); }
					message = messageConsumer.receive(250);
				}
				log.info("Message listener exiting thread.");
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		else {
			log.error("There is no messageConsumer wired into the listener, exiting thread");
		}
	}
	
	public void stopListening() {
		keepListening = false;
	}

}
