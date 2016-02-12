package io.pivotal.pa.rabbitmq.jms.raw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JMSProperties {
	
	@Value("${jms.queue:default.queue}")
	public String queueName;

	@Value("${jms.topic:default.topic}")
	public String topicName;
	
	@Value("${jms.durable-queue:not-durable}")
    public String durableQueue;

	@Value("${jms.priority:-1}")
	public int jmsPriority;

	@Value("${jms.reply-to}")
	public String jmsReplyTo;
	
	@Value("${jms.ttl:-1}")
	public long ttl;

	@Value("${jms.persistent:false}")
	public boolean persistent;
	
	@Value("${jms.ack:1}")
	public String ackModeStr;

}
