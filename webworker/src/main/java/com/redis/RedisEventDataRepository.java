package com.redis;

import com.mongodb.util.JSON;
import com.rabbitMQ.RabbitMqConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Repository
public class RedisEventDataRepository {
    private static final String STRING_EVENT_DATA = "EVENTDATA";

    private HashOperations hashOperations;

    private RedisTemplate<String, RabbitMqConsumer> redisTemplate;

    @Autowired
    public RedisEventDataRepository(RedisTemplate<String, RabbitMqConsumer>  redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    public void save(String queueName, RabbitMqConsumer rabbitMqConsumer) {
        hashOperations.put(STRING_EVENT_DATA, queueName, rabbitMqConsumer);
    }

    public List findAll() {
        return hashOperations.values(STRING_EVENT_DATA);
    }

    public RabbitMqConsumer findByQueueName(String queueName) {
        if(hashOperations.get(STRING_EVENT_DATA, queueName) != null) {
            return (RabbitMqConsumer) hashOperations.get(STRING_EVENT_DATA, queueName);
        } else {
            return null;
        }
    }

    public void update(String queueName, RabbitMqConsumer rabbitMqConsumer) {
        save(queueName, rabbitMqConsumer);
    }

    public void delete(String queueName) {
        hashOperations.delete(STRING_EVENT_DATA, queueName);
    }
}
