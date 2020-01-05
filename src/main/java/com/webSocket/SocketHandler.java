package com.webSocket;

import com.google.gson.Gson;
import com.reddis.RedisWebSocketSessionRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {

    private RedisWebSocketSessionRepository redisWebSocketSessionRepository;

    public static Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public SocketHandler(RedisWebSocketSessionRepository redisWebSocketSessionRepository){
        this.redisWebSocketSessionRepository = redisWebSocketSessionRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //redisWebSocketSessionRepository.save(session);
        String userName = (String) session.getAttributes().get("userName");
        sessions.put(userName, session);
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userName = (String) session.getAttributes().get("userName");
        //redisWebSocketSessionRepository.delete(userName);
        sessions.remove(userName);
        super.afterConnectionClosed(session, status);
    }
}