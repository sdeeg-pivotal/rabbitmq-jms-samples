package io.pivotal.pa.rabbitmq.jms.raw.client;

import java.time.LocalTime;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class SimpleMessageListener implements MessageListener {

	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			try {
				System.out.println(LocalTime.now()+"> The TextMessage is: " + ((TextMessage) message).getText());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(LocalTime.now()+"> The Message is: " + message.toString());
		}
	}

}
