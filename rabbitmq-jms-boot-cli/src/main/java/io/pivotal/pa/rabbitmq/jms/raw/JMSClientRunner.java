package io.pivotal.pa.rabbitmq.jms.raw;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import io.pivotal.pa.rabbitmq.jms.raw.client.JMSClientWorker;
import io.pivotal.pa.rabbitmq.jms.raw.client.MessageConsumerClient;

//Start the worker(s), capture the input, control the worker(s).
@Profile("!usage")
@Component
public class JMSClientRunner implements CommandLineRunner {
	
	@Autowired
	List<JMSClientWorker> workers;
	
	@Autowired
	AbstractApplicationContext context;
	
	//TODO: make this work for multiple workers with a thread for each
	@Override
	public void run(String... arg0) throws Exception {

		System.out.println("Hello from the JMS Runner");

		if(workers != null) {
			if(workers.size() == 1) {
				for(JMSClientWorker worker: workers) {
					worker.initialize();
					worker.start();

					if(worker instanceof MessageConsumerClient) {
						//Grab the input and Exit, Pause, or Resume
						int ch;
						while ((ch = System.in.read()) != -1) {
							if (ch == 'x' || ch == 'X') {
								System.out.println("Exiting");
								worker.stop();
								break;
							}
							else if (ch == 'p' || ch == 'P') {
								System.out.println("Pausing ... (press 'r' to resume)");
								worker.stop();
							}
							else if (ch == 'r' || ch == 'R') {
								System.out.println("Resuming (press 'p' to pause or 'x' to exit)");
								worker.start();
							}
						}
					}
				}
			}
			else {
				System.out.println("I want to run multiple workers, but I can only do a single one now: found "+workers.size()+" workers.");
			}
		}

		System.out.println("Goodbye.");
		context.close();
		System.exit(0);
	}
}
