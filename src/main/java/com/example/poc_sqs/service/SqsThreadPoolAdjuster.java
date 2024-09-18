package com.example.poc_sqs.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

/**
 * Example of optimization
 */
@Slf4j
@Service
public class SqsThreadPoolAdjuster {

    private final SqsAsyncClient sqsClient;
    private String highPriorityQueueUrl;

    private final ThreadPoolTaskExecutor lowPriorityExecutor;

    public SqsThreadPoolAdjuster(SqsAsyncClient sqsClient,
                                 ThreadPoolTaskExecutor lowPriorityExecutor,
                                 @Value("${cloud.aws.sqs.queue.high-priority}") String highPriorityQueue) {
        this.sqsClient = sqsClient;
        this.lowPriorityExecutor = lowPriorityExecutor;
        this.highPriorityQueueUrl = highPriorityQueue;
    }

    @SneakyThrows
    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    public void checkHighPriorityQueue() {
        boolean hasMessages = sqsClient.receiveMessage(
                        ReceiveMessageRequest.builder()
                                .queueUrl(highPriorityQueueUrl)
                                .maxNumberOfMessages(1)
                                .waitTimeSeconds(1)
                                .build())
                .join()
                .hasMessages();


        // High-priority queue is empty, shift resources to low-priority queue
        // High-priority queue has messages, give it more threads
        log.info("Queue: {} hasMessages: {}", highPriorityQueueUrl, hasMessages);
        scaleLowPriorityThreads(!hasMessages);
    }

    private void scaleLowPriorityThreads(boolean noHighPriorityMessages) {
        if (noHighPriorityMessages) {
            lowPriorityExecutor.setMaxPoolSize(20);
            lowPriorityExecutor.setCorePoolSize(10);
        } else {
            lowPriorityExecutor.setMaxPoolSize(5);
            lowPriorityExecutor.setCorePoolSize(2);
        }
    }
}
