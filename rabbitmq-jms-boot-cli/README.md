#RabbitMQ JMSClient Test Application

This is a Spring Boot application that makes use of the JMS API as exposed by the RabbitMQ JMSClient.

Note:  This app has a dependency on a version of the RabbitMQ JMSClient >=1.4.7, which as of this writing (20160204) is the current SNAPSHOT version in the private RabbitMQ repository.

##Running

The client is a Java application.  It is self executing, meaning you can run it with the java -jar command.

eg:
```
java -jar target/rabbitmq-jms-boot-cli-0.0.1-SNAPSHOT.jar
```

(The sample parameter sets below are passed to this command)

##Parameters

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
--jms.priority=[<0-9>]
--jms.reply-to=[<reply-to-queue>]
--jms.ttl=[<time-to-live>]
--message=[default message | <message>]
--delay=[0 | <delay>] (in milliseconds)
--nummessages=[1 | <nummessages>]
--batchsize=[<batchsize>] (turns on transactionality for senders)
--counter=[true | <true|false> ] (turns on/off display of counter in consumer and prepending of counter in senders)
--poison.enabled=[false | <true|false>]
--poison.send-percent=[5 | <0-100>]
--poison.message=[default | <message>]
--poison.try-limit=[1 | <n>]
--poison.backout-queue=[backout.queue | <backout-queue>]
```

Simple send/receive:
```
#Consumer
--spring.profiles.active=consume --amqp.uri=amqp://user:pass@server-name:5672 --jms.queue=my.queue

#Sender
--spring.profiles.active=send --amqp.uri=amqp://user:pass@server-name:5672 --jms.queue=my.queue --nummessages=10
```

##Connecting to existing queues/exchanges

When connecting to existing queues and exchanges the JMS primative may have a configuration mismatch that results in an exception.  In this case use --amqp.queue/exchange instead of --jms.queue/topic.

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

Set the param --batchsize=n to a number greater than 0 and transactions will be turned on for senders.  After every n messages a commit is issued.  (Consumers currently don't support this.)

##Priority queues

JMS priorities are utilized by RabbitMQ priority queues to control delivery.  This means the RabbitMQ queue was created with the attribute x-max-priority=9 (JMS assumes 0-9 priorities).  Users can then set the priority of the messages sent.

```
--spring.profiles.active=send --nummessages=2 --message="priority 9" --jms.priority=9 --amqp.exchange=an.exchange
```

##Reply To queues

The client will set the JMS ReplyTo queue when it's passed the --jms.reply-to flag.

```
--spring.profiles.active=send --nummessages=2 --message="priority 9" --jms.reply-to=my.reply.queue
```

Consuming clients look for a JMSReplyTo queue in the message, and if they find one will echo the message payload to it setting the JMSCorrelationID to the messages ID.

##Poison messages

Enable sending/detection of poison messages by passing the --poison.enable=true parameter.

#Passing parameters in a file

While you can pass all properties as parameters to the app, Spring allows the use of property files that can be loaded with a profile.  You can set any property in the file (see application.yml in the app for the comprehensive list of properties), and this is an easy way to use the client with a remote broker.  Create a file called application-remote.yml and then pass "remote" as part of the active profile set.  To get more information from the client use features in Spring that allows you to easily enable logging for certain modules.

application-remote.yml
```
amqp:
  uri: amqp://user:password@my-server/%2F
logging:
  level:
    io:
      pivotal:
        pa: INFO
jms:
  queue: a.queue
```

running it
```
java -jar target/rabbitmq-jms-boot-cli-0.0.1-SNAPSHOT.jar --spring.profiles.active=sender,remote
```
