package com.konnectnet.core.feed.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedEvent {
    private String postId;
    private String userId;
    private Date createdAt;
}