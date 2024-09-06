package com.example.poc_sqs.service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SqsThreadPoolAdjusterTest {

    @Autowired
    private SqsAsyncClient sqsClient;

    @Value("${cloud.aws.sqs.queue.high-priority}")
    private String highPriorityQueueUrl;

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private ThreadPoolTaskExecutor lowPriorityExecutor;

    @Autowired
    private SqsThreadPoolAdjuster sqsThreadPoolAdjuster;

    @MockBean
    private SqsMessageListener sqsMessageListener;

    @Test
    public void whenHighPriorityQueueIsEmpty_thenLowPriorityThreadsShouldScaleUp() {
        sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(highPriorityQueueUrl).build());
        sqsThreadPoolAdjuster.checkHighPriorityQueue();

        Assertions.assertThat(lowPriorityExecutor.getCorePoolSize()).isEqualTo(10);
        Assertions.assertThat(lowPriorityExecutor.getMaxPoolSize()).isEqualTo(20);
    }

    @Test
    public void whenHighPriorityQueueHasMessages_thenLowPriorityThreadsShouldScaleDown() {
        sqsTemplate.send(to -> to.queue(highPriorityQueueUrl).payload("test message"));
        sqsThreadPoolAdjuster.checkHighPriorityQueue();


        Assertions.assertThat(lowPriorityExecutor.getCorePoolSize()).isEqualTo(2);
        Assertions.assertThat(lowPriorityExecutor.getMaxPoolSize()).isEqualTo(5);
    }
}