package com.konnectnet.core.friend.event;

import com.konnectnet.core.notification.enums.NotificationType;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestEvent implements Serializable {
    private NotificationType type;
    private String content;
    private String senderName;
    private String senderId;
    private String recipientId;
}
