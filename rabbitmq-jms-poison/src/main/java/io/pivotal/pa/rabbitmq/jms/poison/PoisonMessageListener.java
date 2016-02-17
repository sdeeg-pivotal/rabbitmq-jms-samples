package io.pivotal.pa.rabbitmq.jms.poison;

import java.util.HashMap;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

public class PoisonMessageListener implements MessageListener {

	private String poison = "~~Poison~~";
	private int poisonTryLimit = 2;
	
	private Session listenSession;

	private HashMap<String,Integer> messageTryCounts = new HashMap<String,Integer>();
	
	public PoisonMessageListener(Session listenSession, Session replySession) {
		this.listenSession = listenSession;
	}
	
	@Override
	public void onMessage(Message message) {
		try {
			String payload = getPayload(message);
			if(payload.endsWith(poison)) {
				System.out.println("Received poison message");
				handlePoison(message);
			}
			else {
				System.out.println("received message: \""+payload+"\"");
				listenSession.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handlePoison(Message message) throws Exception {
		Integer messageTryCount = new Integer(0);
		if(messageTryCounts.containsKey(message.getJMSMessageID())) {
			messageTryCount = messageTryCounts.get(message.getJMSMessageID());
		}
		messageTryCount++;

		if(messageTryCount < poisonTryLimit) {
			System.out.println("\tTry limit is "+poisonTryLimit+" and messageTryCount is "+messageTryCount+", requeueing and rolling back.");
			messageTryCounts.put(message.getJMSMessageID(), messageTryCount);
			listenSession.rollback();
		}
		else {
			System.out.println("\tTry limit is "+poisonTryLimit+" and messageTryCount is "+messageTryCount+", sending to backout queue and commiting");
			listenSession.commit();
		}
	}
	
	private String getPayload(Message message) throws Exception {
		String payload;
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
			System.out.println("Message not recognized as a TextMessage or BytesMessage.  It is of type: "+message.getClass().toString());
			payload = message.toString();
		}
		return payload;
	}
}
