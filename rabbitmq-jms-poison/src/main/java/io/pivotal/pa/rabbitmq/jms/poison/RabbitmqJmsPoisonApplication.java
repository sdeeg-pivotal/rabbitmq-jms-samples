package io.pivotal.pa.rabbitmq.jms.poison;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

@SpringBootApplication
public class RabbitmqJmsPoisonApplication implements CommandLineRunner {

	@Value("${amqp.uri:amqp://guest:guest@localhost:5672/%2F}")
	private String amqpUri;

	//Setup the connection with the message listener
	@Override
	public void run(String... arg0) throws Exception {
		boolean transacted = true;
		
		RMQConnectionFactory connectionFactory = new RMQConnectionFactory();
		connectionFactory.setUri(amqpUri);

		Connection connection = connectionFactory.createConnection();

		Session listenSession = connection.createSession(transacted, Session.SESSION_TRANSACTED);

		MessageConsumer messageConsumer = listenSession.createConsumer(listenSession.createQueue("test.queue"));

        PoisonMessageListener listener = new PoisonMessageListener(listenSession);

		//Comment this out and use below to make the consumer work
		messageConsumer.setMessageListener(listener);

        connection.start();

        //Use a loop in a single thread to consume the messages.  This works 
        //where registering a MessageListener doesn't.
//        Message message = messageConsumer.receive();
//        while(message != null) {
//        	listener.onMessage(message);
//        	message = messageConsumer.receive();
//        }

		int ch;
		while ((ch = System.in.read()) != -1) {
			if (ch == 'x' || ch == 'X') {
				System.out.println("Exiting");
				break;
			}
		}
		
		connection.stop();
		System.exit(0);
    }
	
	public static void main(String[] args) {
		SpringApplication.run(RabbitmqJmsPoisonApplication.class, args);
	}
}
