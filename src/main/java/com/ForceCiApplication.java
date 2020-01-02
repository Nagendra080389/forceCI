package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.dao")
public class ForceCiApplication {

    @Bean
    JedisConnectionFactory jedisConnectionFactory(){
        String redisUriString = System.getenv("REDIS_URL");

        URI redisUri = URI.create(redisUriString);
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setHostName(redisUri.getHost());
        connectionFactory.setPort(redisUri.getPort());
        return connectionFactory;
    }

    @Bean
    RedisTemplate<String, WebSocketSession> redisTemplate(){
        RedisTemplate<String,WebSocketSession> redisTemplate = new RedisTemplate<String, WebSocketSession>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        return redisTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(ForceCiApplication.class, args);
    }
}
