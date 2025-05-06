package com.konnectnet.core.notification.config;

import lombok.Getter;

@Getter
public enum WebSocketDestinations {
    USER_NOTIFICATION_QUEUE("/queue/notifications");

    private final String destination;

    WebSocketDestinations(String destination) {
        this.destination = destination;
    }

}