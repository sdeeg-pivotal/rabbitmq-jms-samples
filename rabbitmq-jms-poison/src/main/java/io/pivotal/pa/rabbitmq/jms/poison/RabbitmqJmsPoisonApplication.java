package io.pivotal.pa.rabbitmq.jms.poison;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
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
//		Session replySession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		MessageConsumer messageConsumer = listenSession.createConsumer(listenSession.createQueue("test.queue"));
		messageConsumer.setMessageListener(new PoisonMessageListener(listenSession, null));

        connection.start();

		int ch;
		while ((ch = System.in.read()) != -1) {
			if (ch == 'x' || ch == 'X') {
				System.out.println("Exiting");
				break;
			}
			else if (ch == 'p' || ch == 'P') {
				System.out.println("Pausing ... (press 'r' to resume)");
				connection.stop();
			}
			else if (ch == 'r' || ch == 'R') {
				System.out.println("Resuming (press 'p' to pause or 'x' to exit)");
				connection.start();
			}
		}
		
		connection.stop();
		System.exit(0);
    }
	
	public static void main(String[] args) {
		SpringApplication.run(RabbitmqJmsPoisonApplication.class, args);
	}
}
