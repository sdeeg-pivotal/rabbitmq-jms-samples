package io.pivotal.pa.rabbitmq.jms.raw.client;

public interface JMSClientWorker {
    public void initialize() throws Exception;
	public void start() throws Exception;
	public void stop() throws Exception;
}
