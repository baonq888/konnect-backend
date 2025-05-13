package com.konnectnet.core.post.kafka;

import com.konnectnet.core.infrastructure.kafka.KafkaTopics;
import com.konnectnet.core.post.event.FeedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedProducer {

    private final KafkaTemplate<String, FeedEvent> kafkaTemplate;
    private static final String TOPIC = KafkaTopics.FEED_EVENT.getTopicName();

    public void sendFeedEvent(FeedEvent event) {
        kafkaTemplate.send(TOPIC, event.getPostId(), event);
    }
}