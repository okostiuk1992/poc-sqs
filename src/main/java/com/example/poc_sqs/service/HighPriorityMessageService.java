package com.example.poc_sqs.service;

import com.example.poc_sqs.model.Tweet;
import com.example.poc_sqs.repo.TweetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HighPriorityMessageService {
    private final TweetRepository tweetRepository;

    public void processHighPriorityMessage(String message) {
        tweetRepository.save(new Tweet(UUID.randomUUID().toString(), message));
    }
}