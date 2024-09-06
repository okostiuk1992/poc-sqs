package com.example.poc_sqs.repo;

import com.example.poc_sqs.model.Tweet;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TweetRepository {
    private final Map<String, Tweet> persistedTweets = new ConcurrentHashMap<>();

    public void save(Tweet tweetToSave) {
        persistedTweets.put(tweetToSave.id(), tweetToSave);
    }

    public Optional<Tweet> findByMessage(String message) {
        return persistedTweets.values().stream()
                .filter(tweet -> tweet.message().equals(message))
                .findFirst();
    }
}
