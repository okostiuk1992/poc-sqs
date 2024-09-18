package com.example.poc_sqs.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@RequiredArgsConstructor
public class SqsListenerContainerFactoryConfig {
    private final SqsAsyncClient sqsAsyncClient;

    @Bean
    public SqsMessageListenerContainerFactory<Object> highPriorityListenerContainerFactory(ThreadPoolTaskExecutor highPriorityExecutor) {
        return SqsMessageListenerContainerFactory.builder()
            .configure(it ->
                it.componentsTaskExecutor(highPriorityExecutor)
                    .acknowledgementMode(AcknowledgementMode.ON_SUCCESS)
                    .build()
            )
            .sqsAsyncClient(sqsAsyncClient)
            .build();
    }

    @Bean
    public SqsMessageListenerContainerFactory<Object> lowPriorityListenerContainerFactory(ThreadPoolTaskExecutor lowPriorityExecutor) {
        return SqsMessageListenerContainerFactory.builder()
            .configure(it ->
                it.componentsTaskExecutor(lowPriorityExecutor)
                    .acknowledgementMode(AcknowledgementMode.ON_SUCCESS)
                    .build()
            )
            .sqsAsyncClient(sqsAsyncClient)
            .build();
    }
}
