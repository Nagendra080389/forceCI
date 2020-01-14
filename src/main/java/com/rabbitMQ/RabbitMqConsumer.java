package com.rabbitMQ;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

public class RabbitMqConsumer extends SimpleMessageListenerContainer {

    public void startConsumers() throws Exception {
        super.doStart();
    }
}
