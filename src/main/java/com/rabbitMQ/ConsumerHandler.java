package com.rabbitMQ;

public class ConsumerHandler {
    public void handleMessage(String text) {
        System.out.println("Received--------------------------: " + text);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
