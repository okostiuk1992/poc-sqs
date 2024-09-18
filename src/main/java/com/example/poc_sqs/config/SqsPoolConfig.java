package com.example.poc_sqs.config;

import io.awspring.cloud.sqs.MessageExecutionThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SqsPoolConfig {

    @Bean(name = "highPriorityExecutor")
    public ThreadPoolTaskExecutor highPriorityExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadFactory(new MessageExecutionThreadFactory("HighPriority-"));
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.initialize();
        return executor;
    }

    @Bean(name = "lowPriorityExecutor")
    public ThreadPoolTaskExecutor lowPriorityExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadFactory(new MessageExecutionThreadFactory("LowPriority-"));
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.initialize();
        return executor;
    }
}

