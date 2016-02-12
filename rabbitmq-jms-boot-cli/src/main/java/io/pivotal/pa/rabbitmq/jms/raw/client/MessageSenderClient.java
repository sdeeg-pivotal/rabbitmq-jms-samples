package io.pivotal.pa.rabbitmq.jms.raw.client;

import java.time.LocalTime;
import java.util.Random;

import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.jms.admin.RMQDestination;

import io.pivotal.pa.rabbitmq.jms.raw.properties.AppProperties;
import io.pivotal.pa.rabbitmq.jms.raw.properties.JMSProperties;

//This bean is created in the SenderConfig class if appropriate profiles are set.
public class MessageSenderClient implements JMSClientWorker {

	private static Logger log = LoggerFactory.getLogger(MessageSenderClient.class);

	@Autowired
	private Session session;
	
	@Autowired
	MessageProducer messageProducer;
	
	@Autowired
	private AppProperties appProperties;
	
	@Autowired
	private JMSProperties jmsProperties;
	
	@Autowired
	private Random randy;
	
	int messageCounter = 0;

	@Override
	public void initialize() throws Exception {
		if (appProperties.numMessages < appProperties.batchSize) {
			appProperties.batchSize = appProperties.numMessages;
		}
		if (appProperties.batchSize > 0) {
			log.info("Using a batch size of " + appProperties.batchSize + ", turning transactions on.");
		}
		if(jmsProperties.jmsPriority>9) {
			log.warn("jmsPriority is set to a number greater than 9 which is not allow by the spec.  Setting to 9.");
			jmsProperties.jmsPriority = 9;
		} else if(jmsProperties.jmsPriority > -1) {
			log.info("Using JMS priority of "+jmsProperties.jmsPriority);
		}
		if(jmsProperties.jmsReplyTo != null && !"".equals(jmsProperties.jmsReplyTo)) {
			log.info("Using jmsReplyTo="+jmsProperties.jmsReplyTo);
		}
		if(jmsProperties.ttl>0) {
			log.info("Setting time to live in the MessageProducer to "+jmsProperties.ttl);
		    messageProducer.setTimeToLive(jmsProperties.ttl);
		}
		if(appProperties.sendPoisonPercent>0) {
			if(appProperties.sendPoisonPercent > 100) {
				log.warn("sendPercent="+appProperties.sendPoisonPercent+" which is higher than 100.  Setting to 100.");
				appProperties.sendPoisonPercent = 100;
			}
			log.info("Sending poison messages enabled.  Poison=\""+appProperties.poisonMessage+"\" sendPercent="+appProperties.sendPoisonPercent+"%");
		}
	}

	//TODO: make sure this method is reentrant
	public void start() throws Exception {

		if (session != null) {
			System.out.println("Sending " + appProperties.numMessages + " messages with a delay of " + appProperties.delay + " and payload \""
					+ appProperties.messageStr + "\" to destination " + ((RMQDestination)messageProducer.getDestination()).getDestinationName());
			try {
				TextMessage textMessage = session.createTextMessage();
				for (messageCounter = 0; messageCounter < appProperties.numMessages; messageCounter++) {
					
					//Set the message text
					String messageText = appProperties.messageStr;
					//Randomly poison
					if(appProperties.sendPoisonPercent>0) {
						if(appProperties.sendPoisonPercent>randy.nextInt(100)) {
							messageText = appProperties.poisonMessage;
						}
					}
					if(appProperties.showCounter) { messageText = "[" + messageCounter + "]" + messageText; }
					textMessage.setText(messageText);
					
					//Set a few optional features based on parameters
					if(jmsProperties.jmsPriority >= 0) {
						messageProducer.setPriority(jmsProperties.jmsPriority);
					}
					if(jmsProperties.jmsReplyTo != null && !"".equals(jmsProperties.jmsReplyTo)) {
						textMessage.setJMSReplyTo(session.createQueue(jmsProperties.jmsReplyTo));
					}

					if(appProperties.showCounter) {
						System.out.println(LocalTime.now()+"> Sending message [" + messageCounter + "]");
					}
					messageProducer.send(textMessage);

					if (appProperties.batchSize > 0) {
						if ((messageCounter + 1) % appProperties.batchSize == 0) {
							System.out.println(LocalTime.now()+"> Committing transaction");
							session.commit();
						}
					}
					
					if(appProperties.delay > 0) { Thread.sleep(appProperties.delay); }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			log.error("Session is null");
		}
	}

	@Override
	public void stop() throws Exception {
		log.warn("Stop on the message sender is currently not implemented.");
	}

}
