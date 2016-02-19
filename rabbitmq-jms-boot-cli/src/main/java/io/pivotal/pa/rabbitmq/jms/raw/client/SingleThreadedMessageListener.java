package io.pivotal.pa.rabbitmq.jms.raw.client;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//This class is used when looking for poison messages.  It is a single thread that calls the receive
//method of the messageConsumer directly and passes any received messages to SimpleMessageListener
//for processing.  This avoids a race condition in the RMQ management of JMS transactions that results
//in double acking.
public class SingleThreadedMessageListener implements Runnable {

	private static Logger log = LoggerFactory.getLogger(SingleThreadedMessageListener.class);

	@Autowired
	private MessageConsumer messageConsumer;
	
	@Autowired
	private SimpleMessageListener simpleMessageListener;
	
	private boolean keepListening = true;
	
	private int receiveWaitTime = 250;
	
	@Override
	public void run() {

		if(messageConsumer != null) {
			log.info("Starting single threaded message listener loop ...");
			keepListening = true;
			try {
				Message message = messageConsumer.receive(receiveWaitTime);
				while(keepListening) {
					if(message != null) { simpleMessageListener.onMessage(message); }
					message = messageConsumer.receive(receiveWaitTime);
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
