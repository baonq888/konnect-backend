package com.konnectnet.core.friend.kafka;

import com.konnectnet.core.friend.event.FriendRequestEvent;
import com.konnectnet.core.infrastructure.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendNotificationProducer {

    private final KafkaTemplate<String, FriendRequestEvent> kafkaTemplate;
    private static final String TOPIC = KafkaTopics.FRIEND_REQUEST.getTopicName();

    public void sendNotification(FriendRequestEvent event) {
        kafkaTemplate.send(TOPIC, event.getRecipientId(), event);
    }
}