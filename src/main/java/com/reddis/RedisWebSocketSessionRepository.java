package com.reddis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class RedisWebSocketSessionRepository {
    private static final String WEBSOCKETSESSION = "WEBSOCKETSESSION";

    private HashOperations hashOperations;

    private RedisTemplate<String, WebSocketSession> redisTemplate;

    @Autowired
    public RedisWebSocketSessionRepository(RedisTemplate<String, WebSocketSession> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init(){
        hashOperations = redisTemplate.opsForHash();
    }

    public void save(WebSocketSession webSocketSession){
        String userName = (String) webSocketSession.getAttributes().get("userName");
        hashOperations.put(WEBSOCKETSESSION, userName, webSocketSession);
    }
    public List findAll(){
        return hashOperations.values(WEBSOCKETSESSION);
    }

    public WebSocketSession findByUserName(String userName){
        return (WebSocketSession) hashOperations.get(WEBSOCKETSESSION, userName);
    }

    public void update(WebSocketSession webSocketSession){
        save(webSocketSession);
    }

    public void delete(String userName){
        hashOperations.delete(WEBSOCKETSESSION, userName);
    }
}
