package com;

import com.webSocket.SocketHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.dao")
public class ForceCiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForceCiApplication.class, args);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable r = () -> {
            if(SocketHandler.sessions != null && !SocketHandler.sessions.isEmpty()) {
                for (Map.Entry<String, WebSocketSession> stringWebSocketSessionEntry : SocketHandler.sessions.entrySet()) {
                    try {
                        // Dummy ping to keep connection alive.
                        stringWebSocketSessionEntry.getValue().sendMessage(new TextMessage("Dummy Ping"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        scheduler.scheduleAtFixedRate(r, 0, 30, TimeUnit.SECONDS);
    }
}
