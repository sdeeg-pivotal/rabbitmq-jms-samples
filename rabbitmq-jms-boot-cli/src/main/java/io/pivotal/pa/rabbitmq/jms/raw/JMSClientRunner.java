package io.pivotal.pa.rabbitmq.jms.raw;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import io.pivotal.pa.rabbitmq.jms.raw.client.JMSClientWorker;

@Profile("!usage")
@Component
public class JMSClientRunner implements CommandLineRunner {
	
	@Autowired
	List<JMSClientWorker> workers;
	
	@Autowired
	AbstractApplicationContext context;
	
	@Override
	public void run(String... arg0) throws Exception {

		System.out.println("Hello from the JMS Runner");

		if(workers != null) {
			//TODO: make this work for multiple workers with a thread for each
			if(workers.size() == 1) {
				for(JMSClientWorker worker: workers) {
					worker.start();
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
