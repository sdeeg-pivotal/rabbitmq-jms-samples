package io.pivotal.pa.rabbitmq.jms.raw.client;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.pivotal.pa.rabbitmq.jms.raw.properties.AppProperties;

public class SimpleMessageListener implements MessageListener {

	private static Logger log = LoggerFactory.getLogger(SimpleMessageListener.class);
	
	@Autowired
	private Session jmsSession;
	
	@Autowired
	private AppProperties appProperties;
	
	@Autowired(required=false)
	private MessageProducer backoutQueueProducer;
	
	private Map<Destination, MessageProducer> messageProducers = new HashMap<>();
	
	private long messageCounter = 0;
	
	@Override
	public void onMessage(Message message) {

		String payload = null;

		try {
			payload = getPayload(message);

			String outputText = LocalTime.now().toString();
			if(appProperties.showCounter) { outputText += "["+(messageCounter++)+"]"; }
			outputText += "> "+payload;
			System.out.println(outputText);
			
			//poisonTryLimit is our flag to look for poison.
			if(appProperties.poisonTryLimit > 0 && payload.endsWith(appProperties.poisonMessage)) {
				log.info("Received a poison message.");
				handlePoison(message);
			}
			else {
				//TODO: add logic to take into account batch-size parameter
				if(jmsSession.getTransacted()) { jmsSession.commit(); }
				replyToMessageIfNecessary(message.getJMSReplyTo(), message.getJMSMessageID(), payload);
			}
			
			if(appProperties.delay > 0) {
				Thread.sleep(appProperties.delay);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handlePoison(Message message) {
		try {
			int messageTryCount = message.getJMSRedelivered() ? 2 : 1;
			if(appProperties.poisonTryLimit > 2) {
				log.warn("The app currently only supports try limits of 1 and 2.  Setting try-limit to 2");
				appProperties.poisonTryLimit = 2;
			}
			
			if(appProperties.poisonTryLimit == 2 && messageTryCount == 1) { //re-queue the message by issuing a rollback
				log.info("Try limit is "+appProperties.poisonTryLimit+" and messageTryCount is "+messageTryCount+", requeueing.");
				jmsSession.rollback();
			}
			else {
				log.info("Try limit is "+appProperties.poisonTryLimit+" and messageTryCount is "+messageTryCount+", sending to backout queue");
				jmsSession.commit();
				backoutQueueProducer.send(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Look for the replyTo field, if it's there echo the message
	private void replyToMessageIfNecessary(Destination replyTo, String id, String payload) {
		try {
			if(replyTo != null) {
				MessageProducer producer = messageProducers.get(replyTo);
				if(producer == null) {
					log.info("Creating a new MessageProducer to use for replys.  Destination: "+replyTo.toString());
					producer = jmsSession.createProducer(replyTo);
					messageProducers.put(replyTo, producer);
				}
                Message requestMessage = jmsSession.createTextMessage(payload);
                requestMessage.setJMSCorrelationID(id);
                producer.send(requestMessage);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}		
	}
	
	private String getPayload(Message message) throws Exception {
		String payload = null;
		
		if (message instanceof TextMessage) {
			payload = ((TextMessage) message).getText();
		} 
		else if(message instanceof BytesMessage) {
			BytesMessage bMessage = (BytesMessage) message;
			int payloadLength = (int)bMessage.getBodyLength();
			byte payloadBytes[] = new byte[payloadLength];
			bMessage.readBytes(payloadBytes);
			payload = new String(payloadBytes);
		}
		else {
			log.warn("Message not recognized as a TextMessage or BytesMessage.  It is of type: "+message.getClass().toString());
			payload = message.toString();
		}
		return payload;
	}

}
