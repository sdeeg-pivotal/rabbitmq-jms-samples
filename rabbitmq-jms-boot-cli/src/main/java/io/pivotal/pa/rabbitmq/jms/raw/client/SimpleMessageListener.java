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
import org.springframework.beans.factory.annotation.Value;

public class SimpleMessageListener implements MessageListener {

	private static Logger log = LoggerFactory.getLogger(SimpleMessageListener.class);
	
	@Autowired
	private Session session;
	
	@Autowired
	private MessageConsumerClient messageConsumerClient;
	
	@Value("${poison.enabled:false}")
	private boolean poisonEnabled;
	
	@Value("${poison.message}")
	private String poisonMessage;
	
	private Map<Destination, MessageProducer> messageProducers = new HashMap<>();
	
	@Override
	public void onMessage(Message message) {
		String payload = null;

		//Get the payload
		try {
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
			
			if(poisonEnabled && payload.endsWith(poisonMessage)) {
				handlePoison(message);
			}
			else {
				replyToMessage(message.getJMSReplyTo(), message.getJMSMessageID(), payload);
				message.acknowledge();
			}			
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		System.out.println(LocalTime.now()+"> "+payload);
	}
	
	private void handlePoison(Message message) {
		log.info("Received a poison message.  Stopping and restarting the consumer.");
		try {
//			messageConsumerClient.stop();
//			messageConsumerClient.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Look for the replyTo field, if it's there echo the message
	private void replyToMessage(Destination replyTo, String id, String payload) {
		try {
			if(replyTo != null) {
				MessageProducer producer = messageProducers.get(replyTo);
				if(producer == null) {
					log.info("Creating a new MessageProducer to use for replys.  Destination: "+replyTo.toString());
					producer = session.createProducer(replyTo);
					messageProducers.put(replyTo, producer);
				}
                Message requestMessage = session.createTextMessage(payload);
                requestMessage.setJMSCorrelationID(id);
                producer.send(requestMessage);
                producer.close();
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}		
	}

}
