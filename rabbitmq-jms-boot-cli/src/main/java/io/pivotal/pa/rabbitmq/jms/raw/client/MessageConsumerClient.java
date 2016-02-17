package io.pivotal.pa.rabbitmq.jms.raw.client;

import javax.jms.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//This bean is created by the ConsumerConfig class if an appropriate profile is set.
public class MessageConsumerClient implements JMSClientWorker {

	private static Logger log = LoggerFactory.getLogger(MessageConsumerClient.class);

	@Autowired
	private Connection connection;
	
	@Autowired(required = false)
	private SingleThreadedMessageListener singleThreadedMessageListener;
	
	private Thread singleThread = null;

	@Override
	public void initialize() throws Exception {
		if(singleThreadedMessageListener != null) {
			log.info("Found the singleThreadedMessageListener, creating a thread for it");
			singleThread = new Thread(singleThreadedMessageListener);
		}
	}

	@Override
	public void start() throws Exception {
		if (connection != null) {
			connection.start();
			if(singleThread != null) {
				singleThread.start();
			}
		} else {
			log.error("Worker being started, but connection is null");
		}
	}

	@Override
	public void stop() throws Exception {
		if (connection != null) {
			if(singleThreadedMessageListener != null) {
				singleThreadedMessageListener.stopListening();
				Thread.sleep(250); //wait for the listener to stop
			}
			connection.stop();
		} else {
			log.error("Worker being stopped, but connection is null");
		}
	}
}
