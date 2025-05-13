package com.konnectnet.core.feed.service;

import com.konnectnet.core.feed.entity.Feed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedService {
    Page<Feed> getUserFeed(Pageable pageable);
}