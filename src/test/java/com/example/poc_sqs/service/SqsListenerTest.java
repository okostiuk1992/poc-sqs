package com.example.poc_sqs.service;

import com.example.poc_sqs.repo.TweetRepository;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@SuppressWarnings("unused")
public class SqsListenerTest {

    @Autowired
    private SqsMessageListener sqsMessageListener;

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private SqsAsyncClient sqsClient;

    @Value("${cloud.aws.sqs.queue.high-priority}")
    private String highPriorityQueueUrl;

    @SpyBean
    private TweetRepository tweetRepository;

    @BeforeEach
    public void setup() {
        setQueueVisibilityTimeout(highPriorityQueueUrl, 5);
        PurgeQueueRequest purgeQueueRequest = PurgeQueueRequest.builder()
            .queueUrl(highPriorityQueueUrl)
            .build();
        sqsClient.purgeQueue(purgeQueueRequest);
    }

    @Test
    public void consumeHighPriorityMessage() {
        // Given
        doCallRealMethod().when(tweetRepository).save(any());
        doCallRealMethod().when(tweetRepository).findByMessage(any());
        var message = UUID.randomUUID().toString();

        // when
        sqsTemplate.send(to -> to.queue(highPriorityQueueUrl).payload(message));

        //then
        await()
            .atMost(Duration.ofSeconds(1))
            .until(() -> tweetRepository.findByMessage(message).isPresent());

        ReceiveMessageResponse response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
            .queueUrl(highPriorityQueueUrl)
            .maxNumberOfMessages(1)
            .build()).join();
        Assertions.assertFalse(response.hasMessages(), "The high-priority queue should be empty after processing.");
    }

    @Test
    public void consumeHighPriorityMessageWithException() {
        // Given
        doThrow(new RuntimeException("Test exception")).when(tweetRepository).save(any());
        var message = UUID.randomUUID().toString();

        // When
        sqsTemplate.send(to -> to.queue(highPriorityQueueUrl).payload(message));

        // Then
        int expectedProcessingAttempts = 3;
        await()
            .atMost(Duration.ofSeconds(20))
            .untilAsserted(() -> verify(tweetRepository, atLeast(expectedProcessingAttempts)).save(any()));
    }

    public void setQueueVisibilityTimeout(String queueUrl, int timeoutInSeconds) {
        Map<QueueAttributeName, String> attributes = new HashMap<>();
        attributes.put(QueueAttributeName.VISIBILITY_TIMEOUT, String.valueOf(timeoutInSeconds));

        SetQueueAttributesRequest request = SetQueueAttributesRequest.builder()
            .queueUrl(queueUrl)
            .attributes(attributes)
            .build();

        sqsClient.setQueueAttributes(request);
    }
}
