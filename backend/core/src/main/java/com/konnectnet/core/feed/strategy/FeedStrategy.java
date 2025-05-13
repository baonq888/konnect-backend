package com.konnectnet.core.feed.strategy;

import com.konnectnet.core.feed.entity.Feed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedStrategy {
    Page<Feed> getFeedForUser(Pageable pageable);
}