#RabbitMQ JMS Projects

A set of apps used to exercise the JMS api provided by the RabbitMQ JMS Client.

The tracker project is available [here](https://www.pivotaltracker.com/n/projects/1540055)

##Boot CLI Client

Project: [rabbitmq-jms-boot-cli](./rabbitmq-jms-boot-cli)

This is a command line app to test the api.  It is written in Spring Boot, but uses native JMS APIs to do all its work.  The app runs as either a sender/publisher or consumer/subscriber, and supports a variety of features to prove functionality.

##Web Application

Project: [rabbitmq-jms-simple-webapp](rabbitmq-jms-simple-webapp)

Illustrates basic settings to generate JMS objects in JNDI on a Tomcat server.  The app itself needs considerable work, but can do basic functions.

##Spring JMS Application

Project: [rabbitmq-jms-spring-template](rabbitmq-jms-spring-template)

Spring Boot command line application that uses the Spring JMSTemplate to do its work.  Runs as sender or consumer.  Send/consume functionality with a few features (message size, nummessages, etc.).