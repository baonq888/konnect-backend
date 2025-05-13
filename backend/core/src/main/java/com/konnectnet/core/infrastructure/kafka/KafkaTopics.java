package com.konnectnet.core.infrastructure.kafka;

public enum KafkaTopics {
    FRIEND_REQUEST("friend-request-topic"),
    FEED_EVENT("new-post-topic");  // Add this line

    private final String topicName;

    KafkaTopics(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}