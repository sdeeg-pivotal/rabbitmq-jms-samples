package io.pivotal.rabbitmq.jms.boot.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("show-usage")
@Component
public class UsageDisplay implements CommandLineRunner {

	@Override
	public void run(String... arg0) throws Exception {
		System.out.println("Usage: java -jar rabbitmq-jms-springboot.jar --spring.profiles.active=sender|receiver");
		System.out.println("options:");
		System.out.println("\t--amqp.uri=amqp://guest:guest@localhost");
		System.out.println("\t--test.message=my-message");
		System.out.println("\t--test.nummessages=100");
		System.out.println("\t--test.delay=1000 (in ms)");
		System.out.println("\t--test.size=2 (in k bytes)");
		System.out.println("\t--test.queue=some.queue");
	}

}
