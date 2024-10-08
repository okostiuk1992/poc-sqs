package com.example.poc_sqs.service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class SqsMessageListener {
    private final HighPriorityMessageService highPriorityMessageService;

    @SqsListener(
        value = "${cloud.aws.sqs.queue.high-priority}",
        factory = "highPriorityListenerContainerFactory"
    )
    public void handleHighPriorityMessages(String message) {
        log.info("Received High Priority Message {} ", message);
        highPriorityMessageService.processHighPriorityMessage(message);
    }

    @SqsListener(
        value = "${cloud.aws.sqs.queue.low-priority}",
        factory = "lowPriorityListenerContainerFactory"
    )
    public void handleLowPriorityMessages(String message) {
        log.info("Received Low Priority Message {} ", message);
    }
}
