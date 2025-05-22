package com.pvt.project71.services;

import com.pvt.project71.domain.entities.NotificationEntity;
import com.pvt.project71.domain.entities.UserEntity;

import java.util.List;

public interface NotificationService {
    NotificationEntity add(UserEntity receiver, String content);
    List<NotificationEntity> fetchUnread(UserEntity receiver);

    List<NotificationEntity> fetchAll(UserEntity receiver);
    void deleteAllRead(UserEntity receiver);
}
