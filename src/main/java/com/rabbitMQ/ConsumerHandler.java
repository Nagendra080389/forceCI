package com.rabbitMQ;

public class ConsumerHandler {
    public void handleMessage(String text) {
        System.out.println("Received--------------------------: " + text);
    }
}
