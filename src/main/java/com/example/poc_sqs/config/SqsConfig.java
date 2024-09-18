package com.example.poc_sqs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Profile("!test")
@Configuration
public class SqsConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.of(region))
            .build();
    }
}
