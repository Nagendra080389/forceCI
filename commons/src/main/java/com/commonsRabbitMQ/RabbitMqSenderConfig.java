package com.commonsRabbitMQ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

@EnableRabbit
@Configuration
public class RabbitMqSenderConfig {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqSenderConfig.class);

    @Value("${spring.rabbitmq.addresses}")
    private String addressURL;


    @Bean
    public ConnectionFactory connectionFactory() throws URISyntaxException {
        return new CachingConnectionFactory(new URI(addressURL));
    }

    /**
     * Required for executing administration functions against an AMQP Broker
     */
    @Bean
    public AmqpAdmin amqpAdmin() throws URISyntaxException {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplate() throws URISyntaxException {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }


}
