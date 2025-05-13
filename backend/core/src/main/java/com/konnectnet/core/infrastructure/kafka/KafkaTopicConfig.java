package com.konnectnet.core.infrastructure.kafka;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic friendRequestTopic() {
        return TopicBuilder
                .name(KafkaTopics.FRIEND_REQUEST.getTopicName())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic feedEventTopic() {
        return TopicBuilder
                .name(KafkaTopics.FEED_EVENT.getTopicName())
                .partitions(1)
                .replicas(1)
                .build();
    }

}

