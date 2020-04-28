package com.commonsRabbitMQ;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

public class ScheduledRabbitMQConsumer extends SimpleMessageListenerContainer {

    public void startConsumers() throws Exception {
        super.doStart();
    }

    public void stopConsumers() throws Exception {
        super.doStop();
    }

    public void shutDownConsumers() throws Exception {
        super.doShutdown();
    }
}