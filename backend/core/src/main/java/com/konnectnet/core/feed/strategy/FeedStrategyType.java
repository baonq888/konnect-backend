package com.konnectnet.core.feed.strategy;

public enum FeedStrategyType {
    PULL("pullBasedStrategy"),
    PUSH("pushBasedStrategy");

    private final String strategyName;

    FeedStrategyType(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getStrategyName() {
        return strategyName;
    }
}