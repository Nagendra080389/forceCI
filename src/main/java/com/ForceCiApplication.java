package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.dao")
public class ForceCiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForceCiApplication.class, args);
    }
}
