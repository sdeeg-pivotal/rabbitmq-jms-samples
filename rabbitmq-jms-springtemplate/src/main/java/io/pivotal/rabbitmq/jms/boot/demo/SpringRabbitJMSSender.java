package io.pivotal.rabbitmq.jms.boot.demo;

import java.util.Arrays;

import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Profile("send")
@Component
public class SpringRabbitJMSSender implements CommandLineRunner {

	private static final Log log = LogFactory.getLog(SpringRabbitJMSSender.class);

	@Value("${test.message:doh!}")
	String message;
	
	@Value("${test.nummessages:1}")
	int numMessages;
	
	@Value("${test.delay:100}")
	int delay;
	
	//in K
	@Value("${test.size:1}")
	int size;
	
	@Value("${test.queue:test.queue}")
	String queueName;

	@Autowired
	JmsTemplate jmsTemplate;
	
	@Override
	public void run(String... arg0) throws Exception {
		if(jmsTemplate != null) {
			
			log.info("Sending "+numMessages+" messages with payload "+message+" and delay of "+delay);
			if(size > 0) {
				//Upscale the message to the right size
				int messageLength = message.length();
				char[] charArray = new char[size*1000];
				Arrays.fill(charArray, 'X');
				message = new String(charArray);
			}

			// Coerce a javax.jms.MessageCreator
			MessageCreator messageCreator = (Session session) -> {
				return session.createTextMessage(message);
			};

			// And publish to a RabbitMQ Queue using Spring's JmsTemplate
			for(int c=0; c<numMessages; c++) {
				jmsTemplate.send(queueName, messageCreator);
				Thread.sleep(delay);
			}
		}
		else {System.out.println("jmsTemplate is null");}
	}

}
