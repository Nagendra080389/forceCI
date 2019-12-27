package com.webSocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    @Value("${websocket.MQTT.hostName}")
    String hostname;

    @Value("${websocket.MQTT.port}")
    int port;

    @Value("${websocket.MQTT.userName}")
    String username;

    @Value("${websocket.MQTT.password}")
    String password;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
                .setRelayHost(hostname).setRelayPort(port).setSystemLogin(username).setSystemPasscode(password).setClientLogin(username).setClientPasscode(password);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect2Deploy/socket").setAllowedOrigins("*").withSockJS();
    }

}