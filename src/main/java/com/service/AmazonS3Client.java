package com.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class AmazonS3Client {

    /*@Value("${amazons3.region}")
    private String region;
    @Value("${amazons3.bucketname}")
    private String bucketName;
    @Value("${amazons3.accessKey}")
    private String accessKey;
    @Value("${amazons3.accessSecret}")
    private String accessSecret;

    @Bean
    public AmazonS3 amazonClient() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey,accessSecret);
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }*/
}
