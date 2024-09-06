package com.example.poc_sqs.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.util.Collections;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Slf4j
@Profile("test")
@Configuration
@Testcontainers
public class TestSqsConfig {
    public static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
            .withServices(LocalStackContainer.Service.SQS)
            .withTmpFs(Collections.singletonMap("/testtmpfs", "rw"))
            .withLogConsumer(new Slf4jLogConsumer(log))
            .withReuse(true);

    @PostConstruct
    public void startLocalStack() {
        if (!localstack.isRunning()) {
            localstack.start();
        }
    }

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public SqsAsyncClient sqsClient() {
        return SqsAsyncClient.builder()
                .endpointOverride(localstack.getEndpointOverride(SQS))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("accesskey", "secretkey")
                ))
                .build();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        if (localstack.isRunning()) {
            registry.add("cloud.aws.sqs.endpoint", () -> localstack.getEndpointOverride(SQS).toString());
        }
    }
}
