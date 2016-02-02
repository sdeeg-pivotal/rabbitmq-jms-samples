package io.pivotal.pa.simplejms;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

public class SenderTopic {

	@Resource(lookup = "java:comp/env/jms/ConnectionFactory")
	private static ConnectionFactory connectionFactory;
	
	@Resource(lookup = "java:comp/env/jms/VisaJMSQueue")
	private static Destination dest;
	
	public String doSend() {		
		String retVal = "run method in Sender";
		
		if(connectionFactory != null && dest != null) {
			retVal = "connectionFactory and dest are there";
		}
		else {
			retVal = "CDI didn't work, getting from Context";
			retVal += " ("+ getFromContext() + ") ";
		}

		try {
			retVal += "sending on a topic ... ";
			Connection connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(dest);
			TextMessage msg=session.createTextMessage();
			msg.setText("My JMS Client");
			producer.send(msg);
			producer.close();
			connection.close();
			retVal += "success";
		} catch(Exception e) { retVal += e.toString(); }

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
