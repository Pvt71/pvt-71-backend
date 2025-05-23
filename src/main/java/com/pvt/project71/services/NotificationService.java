package com.pvt.project71.services;

import com.pvt.project71.domain.entities.NotificationEntity;
import com.pvt.project71.domain.entities.UserEntity;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    NotificationEntity add(UserEntity receiver, String content);

    List<NotificationEntity> fetchAll(UserEntity receiver);

    void removeNotification(UUID uuid, UserEntity user);

    boolean anyNotificationsLeft(UserEntity user);
}
