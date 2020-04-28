package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = {"com.dao", "com.backgroundworker.quartzJob", "com.commonsRabbitMQ"})
public class ForceCiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForceCiApplication.class, args);
    }
}
