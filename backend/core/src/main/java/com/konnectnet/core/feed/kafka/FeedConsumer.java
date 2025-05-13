package com.konnectnet.core.feed.kafka;

import com.konnectnet.core.feed.entity.Feed;
import com.konnectnet.core.feed.repository.FeedRepository;
import com.konnectnet.core.feed.event.FeedEvent;
import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.repository.UserRepository;
import com.konnectnet.core.post.entity.Post;
import com.konnectnet.core.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedConsumer {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @KafkaListener(topics = "feed-event-topic", groupId = "feed-group")
    public void consumeFeedEvent(FeedEvent event) {
        log.info("Received FeedEvent: {}", event);

        UUID postId = UUID.fromString(event.getPostId());
        UUID userId = UUID.fromString(event.getUserId());

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        Feed feed = new Feed();
        feed.setUser(user);
        feed.setPost(post);
        feed.setAuthorId(post.getUser().getId());

        feedRepository.save(feed);
        log.info("Saved Feed entry for user {} and post {}", userId, postId);
    }
}