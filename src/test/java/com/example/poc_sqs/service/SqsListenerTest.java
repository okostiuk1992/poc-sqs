package com.example.poc_sqs.service;

import com.example.poc_sqs.repo.TweetRepository;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;

@SpringBootTest
public class SqsListenerTest {

    @Autowired
    private SqsMessageListener sqsMessageListener;

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private SqsAsyncClient sqsClient;

    @Value("${cloud.aws.sqs.queue.high-priority}")
    private String highPriorityQueueUrl;

    @Autowired
    private TweetRepository tweetRepository;


    @Test
    @SneakyThrows
    public void whenHighPriorityQueueHasMessages_thenLowPriorityThreadsShouldScaleDown() {
        var message = UUID.randomUUID().toString();
        // when
        sqsTemplate.send(to -> to.queue(highPriorityQueueUrl).payload(message));

        //then
        await().atMost(Duration.ofSeconds(1))
                .until(() -> tweetRepository.findByMessage(message).isPresent());

        ReceiveMessageResponse response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(highPriorityQueueUrl)
                .maxNumberOfMessages(1)
                .build()).join();
        Assertions.assertFalse(response.hasMessages(), "The high-priority queue should be empty after processing.");
    }
}