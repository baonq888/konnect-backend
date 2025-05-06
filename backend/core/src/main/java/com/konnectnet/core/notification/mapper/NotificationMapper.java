package com.konnectnet.core.notification.mapper;

import com.konnectnet.core.notification.dto.NotificationDTO;
import com.konnectnet.core.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "recipient.id", target = "recipientId")
    NotificationDTO toNotificationDTO(Notification notification);

}