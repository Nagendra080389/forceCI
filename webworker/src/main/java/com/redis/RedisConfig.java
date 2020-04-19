package com.redis;

import com.rabbitMQ.RabbitMqConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.URI;

@Configuration
@ComponentScan("com.controller")
public class RedisConfig {

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        String redisUriString = System.getenv("REDIS_URL");

        URI redisUri = URI.create(redisUriString);
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setHostName(redisUri.getHost());
        connectionFactory.setPort(redisUri.getPort());
        connectionFactory.setPassword(redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(":") + 1));
        return connectionFactory;
    }

    @Bean
    RedisTemplate<String, RabbitMqConsumer> redisTemplate() {
        RedisTemplate<String, RabbitMqConsumer>  redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        return redisTemplate;
    }

}
