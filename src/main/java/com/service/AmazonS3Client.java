package com.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Configuration
public class AmazonS3Client {

    @Value("${amazons3.region}")
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
    }
}
