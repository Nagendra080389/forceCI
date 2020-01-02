package com.reddis;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Repository
public class RedisWebSocketSessionRepository {
    private HashOperations hashOperations;

    private RedisTemplate redisTemplate;

    public RedisWebSocketSessionRepository(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
        this.hashOperations = this.redisTemplate.opsForHash();
    }

    public void save(WebSocketSession webSocketSession){
        String userName = (String) webSocketSession.getAttributes().get("userName");
        hashOperations.put("WEBSOCKETSESSION", userName, webSocketSession);
    }
    public List findAll(){
        return hashOperations.values("WEBSOCKETSESSION");
    }

    public WebSocketSession findByUserName(String userName){
        return (WebSocketSession) hashOperations.get("WEBSOCKETSESSION", userName);
    }

    public void update(WebSocketSession webSocketSession){
        save(webSocketSession);
    }

    public void delete(String userName){
        hashOperations.delete("WEBSOCKETSESSION", userName);
    }
}
