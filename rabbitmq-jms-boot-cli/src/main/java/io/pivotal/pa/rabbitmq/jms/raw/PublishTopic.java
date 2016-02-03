package io.pivotal.pa.rabbitmq.jms.raw;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"publish", "publish-topic"})
@Component
public class PublishTopic implements JMSTest {

	private static Logger log = LoggerFactory.getLogger(PublishTopic.class);

	@Autowired
	private Session session;
	
	@Value("${topic:default.topic.name}")
	private String topicName;
	
	@Value("${message:default}")
	private String messageStr;
	
	@Value("${nummessages:0}")
	private int numMessages;
	
	@Value("${delay:100}")
	private long delay;
	
	@Override
	public void run() throws Exception {
		if(session != null) {
			Topic topic = session.createTopic(topicName);
			TextMessage message = session.createTextMessage();
			MessageProducer sender = session.createProducer(topic);
			System.out.println("Sending message \""+messageStr+"\" to topic "+topicName);
			try {
				for(int c=0; c<numMessages; c++) {
					message.setText("["+c+"] "+messageStr);
					sender.send(message);
					Thread.sleep(delay);
				}
			} catch(Exception ignore) {}
		} else {
			log.error("session is null");
		}
	}

}
