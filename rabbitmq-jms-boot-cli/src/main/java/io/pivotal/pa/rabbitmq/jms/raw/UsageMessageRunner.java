package io.pivotal.pa.rabbitmq.jms.raw;

import javax.jms.Session;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("usage")
@Component
public class UsageMessageRunner implements CommandLineRunner {

	//TODO: Show contents of help file
	@Override
	public void run(String... arg0) throws Exception {
		System.out.println("Usage: <show usage message>");
		System.out.println("--spring.profiles.active=[send | consume | publish | subscribe");
		System.out.println("--jms.host=[localhost | <host>]");
		System.out.println("--jms.username=[guest | <username>]");
		System.out.println("--jms.password=[guest | <password>]");
		System.out.println("--jms.port=[5672 | <port>]");
		System.out.println("--jms.ack=[AUTO_ACKNOWLEDGE | CLIENT_ACKNOWLEDGE | DUPS_OK_ACKNOWLEDGE | SESSION_TRANSCTION]" );
		System.out.println("--delay=[0 | <delay>]");
		System.out.println("--nummessages=[1 | <nummessages>]");
		System.out.println("--queue=[test.queue | <queue>]");
		System.out.println("--durable=[false  | <true>]");
		System.out.println("--batchsize=[-1 | <batchsize>]");
	}

}
