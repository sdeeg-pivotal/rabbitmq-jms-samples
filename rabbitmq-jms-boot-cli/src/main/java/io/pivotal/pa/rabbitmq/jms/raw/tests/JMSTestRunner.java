package io.pivotal.pa.rabbitmq.jms.raw.tests;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

@Profile("!usage")
@Component
public class JMSTestRunner implements CommandLineRunner {
	
	@Autowired
	List<JMSTest> tests;
	
	@Autowired
	AbstractApplicationContext context;
	
	@Override
	public void run(String... arg0) throws Exception {

		System.out.println("Hello from the JMS Runner");

		if(tests != null) {
			//TODO: make this work for multiple tests with a thread for each
			if(tests.size() == 1) {
				for(JMSTest test : tests) {
					test.run();
				}
			}
			else {
				System.out.println("I want to run multiple tests, but I can only do a single test now: found "+tests.size()+" tests.");
			}
		}

		System.out.println("Goodbye.");
		context.close();
		System.exit(0);
	}
}
