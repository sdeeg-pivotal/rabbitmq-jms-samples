#RabbitMQ JMSClient Test Application

This is a Spring Boot application that makes use of the JMS API as exposed by the RabbitMQ JMSClient.

##Usage:

###Running

The client is a Java application.  It is "self-executing", meaning you can run it with the java -jar command.

eg:
```
java -jar target/rabbitmq-jms-boot-cli-0.0.1-SNAPSHOT.jar
```

(The sample parameter sets assume this as the command)

###Parameters

When run with no parameters it prints its usage message.

```
Parameters:
--spring.profiles.active=[send | consume | publish | subscribe]
--amqp.uri=[<uri>] (overrides amqp.host, etc)
--amqp.host=[localhost | <host>]
--amqp.username=[guest | <username>]
--amqp.password=[guest | <password>]
--amqp.port=[5672 | <port>]
--amqp.vhost=[/ | <vhost>]
--amqp.exchange=[<exchange>] (messages go to the specified exchange)
--amqp.queue=[<queue>] (listeners attach to the specified queue)
--jms.ack=[AUTO_ACKNOWLEDGE | CLIENT_ACKNOWLEDGE | DUPS_OK_ACKNOWLEDGE | SESSION_TRANSCTION]
--jms.queue=[test.queue | <queue>]
--jms.topic=[test.topic | <topic>]
--jms.durable-queue=[<durable queue name>] (turns on use of durable subscriber)
--jms.persistent=[false | <true|false>]
--message=[default message | <message>]
--delay=[0 | <delay>] (in milliseconds)
--nummessages=[1 | <nummessages>]
--batchsize=[<batchsize>] (turns on transactionality for senders)
```

Simple send/receive:
```
#Consumer
--spring.profiles.active=consume --amqp.uri=amqp://user:pass@server-name:5672 --jms.queue=my.queue

#Sender
--spring.profiles.active=send --amqp.uri=amqp://user:pass@server-name:5672 --jms.queue=my.queue --nummessages=10
```

##Using Consistent Hash Exchanges

The consistent hash exchange plugin enables an exchange to distribute load across queues.  (Round robin delivery is supported 
for consumers on a queue, but not in exchanges to route to queues.)  First the plugin needs to be enabled on the target brokers in the normal way.

```
rabbitmq-plugins enable rabbitmq_consistent_hash_exchange
```

Queues should be declared and bound in the usual way.  Make sure to use a numeric routing key for the binding as this sets the "weight" that the plugin uses to send messages to bound queues.  (equal numbers, equal weight.  Different numbers, proportionally different weights.)  To send messages to the exchange pass amqp.exchange to the application instead of jms.queue/topic.

```
--spring.profiles.active=send --amqp.uri=amqp://user:pass@server-name:5672 --amqp.exchange=an.exchange --nummessages=10
```

##Transactions

Set the param --batchsize to a number greater than 0 and transactions will be turned on for senders.  Consumers currently don't support this.
