package io.pivotal.pa.rabbitmq.jms.raw;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

@Profile("!usage")
@Component
public class ApplicationRunner implements CommandLineRunner {
	
	@Autowired
	List<JMSTestRunner> tests;
	
	@Autowired
	AbstractApplicationContext context;
	
	@Override
	public void run(String... arg0) throws Exception {

		System.out.println("Hello from the JMS Runner");

		if(tests != null) {
			//TODO: make this work for multiple tests with a thread for each
			if(tests.size() == 1) {
				for(JMSTestRunner test : tests) {
					test.run();
				}
			}
			else {
				System.out.println("I want to run a single test, but I found "+tests.size());
			}
		}

		System.out.println("Goodbye.");
		context.close();
		System.exit(0);
	}
}
