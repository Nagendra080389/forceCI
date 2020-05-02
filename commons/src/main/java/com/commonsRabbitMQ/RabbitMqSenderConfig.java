package com.commonsRabbitMQ;

import com.backgroundworker.quartzJob.SFDCScheduledConnectionDetailsMongoRepository;
import com.backgroundworker.quartzJob.ScheduledDeploymentMongoRepository;
import com.backgroundworker.quartzJob.ScheduledLinkedServicesMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

import static com.backgroundworker.quartzJob.SchedulerConfig.SCHEDULED_JOB_EXCHANGE;
import static com.backgroundworker.quartzJob.SchedulerConfig.SCHEDULED_QUEUE_NAME;

@EnableRabbit
@Configuration
public class RabbitMqSenderConfig {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqSenderConfig.class);

    @Value("${spring.rabbitmq.addresses}")
    private String addressURL;

    @Bean
    Queue queue() {
        return new Queue(SCHEDULED_QUEUE_NAME, false);
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(SCHEDULED_JOB_EXCHANGE);
    }

    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(SCHEDULED_QUEUE_NAME);
    }

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
