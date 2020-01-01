package com.webSocket;

import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
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

    public static Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("session - > "+session.getId());
        System.out.println("session - > "+session.getAcceptedProtocol());
        for (Map.Entry<String, Object> stringObjectEntry : session.getAttributes().entrySet()) {
            System.out.println("session m - > "+stringObjectEntry.getValue() + " -- " + stringObjectEntry.getKey());
        }
        System.out.println("session - > "+session.getHandshakeHeaders());
        System.out.println("session - > "+session.getLocalAddress().getHostName());
        System.out.println("session - > "+session.getLocalAddress().getHostString());
        System.out.println("session - > "+session.getLocalAddress().getAddress());
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions = null;
        super.afterConnectionClosed(session, status);
    }
}