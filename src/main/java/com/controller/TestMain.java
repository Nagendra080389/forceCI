package com.controller;

import com.rabbitMQ.DeploymentJob;
import com.rabbitMQ.RabbitMqSenderConfig;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectorConfig;
import com.utils.SFDCUtils;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestMain {

    public static void main(String[] args) throws Exception {
        RabbitMqSenderConfig rabbitMqSenderConfig = new RabbitMqSenderConfig();
        AmqpAdmin amqpAdmin = rabbitMqSenderConfig.amqpAdmin();
        System.out.println(amqpAdmin.getQueueProperties("182022017_master"));
    }

    public void execute() throws FileNotFoundException {

    }
}
