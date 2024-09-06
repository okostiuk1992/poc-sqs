package com.example.poc_sqs.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.util.Collections;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Slf4j
@Profile("test")
@Configuration
public class SqsQueueConfig {
    @Autowired
    private SqsAsyncClient sqsClient;

    @Value("${cloud.aws.sqs.queue.high-priority}")
    private String highPriorityQueueUrl;

    @PostConstruct
    public void setUp() {
        sqsClient.createQueue(CreateQueueRequest.builder().queueName(highPriorityQueueUrl).build());
    }

}
