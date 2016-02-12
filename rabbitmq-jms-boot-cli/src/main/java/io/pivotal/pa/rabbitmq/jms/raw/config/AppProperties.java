package io.pivotal.pa.rabbitmq.jms.raw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//Class to hold all the application properties.  Create once, inject everywhere.
@Component
public class AppProperties {

	@Value("${delay}")
	public long delay;

	@Value("${nummessages}")
	public long numMessages;
	
	@Value("${message}")
	public String messageStr;
	
	@Value("${message-size}")
	public long messageSize;
	
	@Value("${batchsize}")
	public long batchSize;
	
	@Value("${poison.send-percent}")
	public int sendPoisonPercent;
	
	@Value("${poison.message}")
	public String poisonMessage;
	
	@Value("${poison.try-limit}")
	public int poisonTryLimit;
	
	@Value("${poison.backout-queue}")
	public String poisonBackoutQueue;

	@Value("${counter}")
	public boolean showCounter;

}
