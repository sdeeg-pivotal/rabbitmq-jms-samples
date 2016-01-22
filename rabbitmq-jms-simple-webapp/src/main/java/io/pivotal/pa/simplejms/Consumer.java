package io.pivotal.pa.simplejms;

import javax.annotation.Resource;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

public class Consumer {

	@Resource(lookup = "java:comp/env/jms/ConnectionFactory")
	private static ConnectionFactory connectionFactory;
	
	@Resource(lookup = "java:comp/env/jms/VisaJMSQueue")
	private static Destination dest;
	

	public String doConsume() {		
		String retVal = "run method in Sender";
		
		if(connectionFactory != null && dest != null) {
			retVal = "connectionFactory and dest are there";
		}
		else {
			retVal = "CDI didn't work, getting from Context";
			retVal += " ("+ getFromContext() + ") ";
		}

		try {
//			retVal += "initiating ... ";
			Connection connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageConsumer consumer = session.createConsumer(dest);
			if(consumer == null) { retVal += " consumer is null!!!"; }
			connection.start();
//			retVal += "consuming ... ";
		    Message m = consumer.receive(1); 
//			retVal += "getting data ... ";
		    if (m != null) {
		        if (m instanceof TextMessage) { 
		            retVal += ((TextMessage) m).getText(); 
		        }
		        else if(m instanceof BytesMessage) {
		        	byte bytes[] = new byte[(int) ((BytesMessage)m).getBodyLength()];
		        	((BytesMessage)m).readBytes(bytes);
		        	retVal += new String(bytes);
		        }
		        else { retVal += m.getClass().getName(); }
		    }
		    else { retVal += "No message"; }
//			retVal += "shutting down ... ";
		    consumer.close();
		    connection.stop();
		    connection.close();
		} catch(Exception e) { e.printStackTrace(); }

		return retVal;
	}

	private String getFromContext()
	{
		String retVal = "rawJMS";
		try {
			InitialContext initCtx = new InitialContext();
			Context envContext = (Context) initCtx.lookup("java:comp/env");
			connectionFactory = (ConnectionFactory) envContext.lookup("jms/VisaJMSConnectionFactory");
			dest = (Destination) envContext.lookup("jms/VisaJMSDestination");
			retVal = "success";
		} catch(Exception e) { retVal = e.toString(); }
		return retVal;
	}

}
