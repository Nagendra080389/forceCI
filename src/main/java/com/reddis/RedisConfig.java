package com.reddis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

@Configuration
@ComponentScan("com.reddis")
public class RedisConfig {

    @Bean
    JedisConnectionFactory jedisConnectionFactory(){
        String redisUriString = System.getenv("REDIS_URL");

        URI redisUri = URI.create(redisUriString);
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setHostName(redisUri.getHost());
        connectionFactory.setPort(redisUri.getPort());
        System.out.println("redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(\":\")+1 - > "+redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(":")+1));
        connectionFactory.setPassword(redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(":")+1));
        return connectionFactory;
    }

    @Bean
    RedisTemplate<String, WebSocketSession> redisTemplate(){
        RedisTemplate<String,WebSocketSession> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        return redisTemplate;
    }

}
