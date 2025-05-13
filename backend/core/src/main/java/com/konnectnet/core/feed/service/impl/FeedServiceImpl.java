package com.konnectnet.core.feed.service.impl;

import com.konnectnet.core.feed.entity.Feed;
import com.konnectnet.core.feed.service.FeedService;
import com.konnectnet.core.feed.strategy.FeedStrategy;
import com.konnectnet.core.feed.strategy.FeedStrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.konnectnet.core.feed.strategy.FeedStrategyType.*;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final Map<String, FeedStrategy> strategyMap;

    @Override
    public Page<Feed> getUserFeed(Pageable pageable) {
        String strategyKey = PULL.getStrategyName();
        FeedStrategy strategy = strategyMap.get(strategyKey);

        if (strategy == null) {
            throw new IllegalArgumentException("Unknown feed strategy: " + strategyKey);
        }

        return strategy.getFeedForUser(pageable);
    }

}