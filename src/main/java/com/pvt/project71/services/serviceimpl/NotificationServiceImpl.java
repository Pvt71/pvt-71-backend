package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.NotificationEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.NotificationRepository;
import com.pvt.project71.services.NotificationService;
import com.pvt.project71.services.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {
    private NotificationRepository notificationRepository;
    private UserService userService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Override
    //@Transactional
    public NotificationEntity add(UserEntity receiver, String content) {
        NotificationEntity notificationEntity = NotificationEntity.builder().content(content)
                .receiver(receiver).receivedAt(LocalDateTime.now()).build();
        int attempt = 0;
        boolean saved = false;
        while (attempt <= 3 && !saved) {
            try {
                notificationEntity.setUuid(UUID.randomUUID());
                notificationEntity = notificationRepository.save(notificationEntity);
                saved = true;
            }catch (DataIntegrityViolationException dataIntegrityViolationException) {
                attempt++;
            }
        } if (!saved) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unique Id cant be made");
        }
        receiver = userService.loadTheLazy(receiver);
        receiver.getNotifications().add(notificationEntity);
        receiver.setNewNotifications(true);
        userService.save(receiver);
        return notificationEntity;
    }

    @Override
    public List<NotificationEntity> fetchUnread(UserEntity receiver) {
        List<NotificationEntity> notificationEntities = notificationRepository.fetchUnread(receiver.getEmail());
        for (NotificationEntity n : notificationEntities) {
            n.setRead(true);
        }
        notificationRepository.saveAll(notificationEntities);
        receiver.setNewNotifications(false);
        userService.save(receiver);
        return notificationEntities;
    }

    @Override
    public List<NotificationEntity> fetchAll(UserEntity receiver) {
        List<NotificationEntity> notificationEntities = notificationRepository.fetchAll(receiver.getEmail());
        for (NotificationEntity n : notificationEntities) {
            n.setRead(true);
        }
        notificationRepository.saveAll(notificationEntities);
        receiver.setNewNotifications(false);
        userService.save(receiver);
        return notificationEntities;
    }

    @Override
    public void deleteAllRead(UserEntity receiver) {
        notificationRepository.deleteAllRead(receiver.getEmail());
    }
}
