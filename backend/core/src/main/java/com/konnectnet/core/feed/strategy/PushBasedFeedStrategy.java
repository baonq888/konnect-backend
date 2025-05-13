package com.konnectnet.core.feed.strategy;

import com.konnectnet.core.auth.repository.UserRepository;
import com.konnectnet.core.feed.entity.Feed;
import com.konnectnet.core.feed.repository.FeedRepository;
import com.konnectnet.core.auth.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("pushBasedStrategy")
@RequiredArgsConstructor
public class PushBasedFeedStrategy implements FeedStrategy {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    @Override
    public Page<Feed> getFeedForUser(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return feedRepository.findByUserOrderByAddedAtDesc(user, pageable);
    }
}