package com.backgroundworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.backgroundworker", "com.commonsRabbitMQ"})
@EnableScheduling
@EnableMongoRepositories(basePackages = {"com.backgroundworker.quartzJob"})
public class BackgroundworkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackgroundworkerApplication.class, args);
    }

}
