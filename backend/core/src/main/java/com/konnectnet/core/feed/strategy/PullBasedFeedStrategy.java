package com.konnectnet.core.feed.strategy;

import com.konnectnet.core.auth.repository.UserRepository;
import com.konnectnet.core.feed.entity.Feed;
import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.post.entity.Post;
import com.konnectnet.core.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("pullBasedStrategy")
@RequiredArgsConstructor
public class PullBasedFeedStrategy implements FeedStrategy {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public Page<Feed> getFeedForUser(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<Post> posts = postRepository.findRecentPostsFromFollowedUsers(user.getId(), pageable);

        return posts.map(post -> {
            Feed feed = new Feed();
            feed.setUser(user);
            feed.setPost(post);
            feed.setAuthorId(post.getUser().getId());
            feed.setAddedAt(post.getCreatedAt());
            return feed;
        });
    }
}