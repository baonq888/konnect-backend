package com.konnectnet.core.infrastructure.kafka;

import lombok.Getter;

@Getter
public enum KafkaTopics {
    FRIEND_REQUEST("friend-request-topic");

    private final String topicName;

    KafkaTopics(String topicName) {
        this.topicName = topicName;
    }

}