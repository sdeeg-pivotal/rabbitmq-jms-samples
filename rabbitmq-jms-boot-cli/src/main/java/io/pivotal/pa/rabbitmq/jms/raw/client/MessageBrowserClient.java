package io.pivotal.pa.rabbitmq.jms.raw.client;

import java.util.Enumeration;
import java.util.Random;

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.jms.admin.RMQDestination;

import io.pivotal.pa.rabbitmq.jms.raw.properties.AppProperties;
import io.pivotal.pa.rabbitmq.jms.raw.properties.JMSProperties;

//This bean is created in the SenderConfig class if appropriate profiles are set.
public class MessageBrowserClient implements JMSClientWorker {

	private static Logger log = LoggerFactory.getLogger(MessageBrowserClient.class);

	@Autowired
	private Session jmsSession;

	@Autowired
	QueueBrowser queueBrowser;

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
		if (jmsProperties.jmsPriority > 9) {
			log.warn("jmsPriority is set to a number greater than 9 which is not allow by the spec.  Setting to 9.");
			jmsProperties.jmsPriority = 9;
		} else if (jmsProperties.jmsPriority > -1) {
			log.info("Using JMS priority of " + jmsProperties.jmsPriority);
		}
		if (jmsProperties.jmsReplyTo != null && !"".equals(jmsProperties.jmsReplyTo)) {
			log.info("Using jmsReplyTo=" + jmsProperties.jmsReplyTo);
		}
		if (appProperties.sendPoisonPercent > 0) {
			if (appProperties.sendPoisonPercent > 100) {
				log.warn("sendPercent=" + appProperties.sendPoisonPercent
						+ " which is higher than 100.  Setting to 100.");
				appProperties.sendPoisonPercent = 100;
			}
			log.info("Sending poison messages enabled.  Poison=\"" + appProperties.poisonMessage + "\" sendPercent="
					+ appProperties.sendPoisonPercent + "%");
		}
	}

	// TODO: make sure this method is reentrant
	public void start() throws Exception {

		if (queueBrowser != null) {
			String queueName = ((RMQDestination) queueBrowser.getQueue()).getQueueName();
			System.out.println(
					"Browsing " +  " messages with a delay of " + appProperties.delay
							+  "\" from destination " + queueName);

			try {
				Queue queue = queueBrowser.getQueue();
				Enumeration msgs = queueBrowser.getEnumeration();

				if (!msgs.hasMoreElements()) {
					System.out.println("No more messages to browse in queue");
				} else {
					while (msgs.hasMoreElements()) {
						Message tempMsg =  (Message)msgs.nextElement();
						System.out.println("Browsing Message: " + tempMsg);
					}
				}

				if (appProperties.delay > 0) {
					Thread.sleep(appProperties.delay);
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
