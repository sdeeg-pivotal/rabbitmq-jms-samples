#Poison Message Consumer

This JMS consumer application uses transactions to implement the concept of rejecting a message.  When it receives a message with a specific payload, "~~Poison~~" by default, it'll either:

1) On first delivery reject the message by calling rollback() on the session.
2) On second delivery the message will be acked by calling commit() on the session

* Non-poison messages trigger commit()

If messages are delivered slowly the client can manage them.  If they are delivered quickly (no delay) an exception is caused.

Poison messages can be generated with the [rabbitmq-jms-boot-cli](../rabbitmq-jms-boot-cli) client by passing --poison.send-percent=[1-100] (the default is 0).