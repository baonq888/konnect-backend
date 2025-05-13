package com.konnectnet.core.feed.service.impl;

import com.konnectnet.core.feed.entity.Feed;
import com.konnectnet.core.feed.service.FeedService;
import com.konnectnet.core.feed.strategy.FeedStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedStrategy feedStrategy;

    @Override
    public Page<Feed> getUserFeed(Pageable pageable) {
        return feedStrategy.getFeedForUser(pageable);
    }
}