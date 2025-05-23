package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.NotificationEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.NotificationRepository;
import com.pvt.project71.services.ChallengeAttemptService;
import com.pvt.project71.services.NotificationService;
import com.pvt.project71.services.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {
    private NotificationRepository notificationRepository;
    private UserService userService;
    private ChallengeAttemptService challengeAttemptService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserService userService, @Lazy ChallengeAttemptService challengeAttemptService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.challengeAttemptService = challengeAttemptService;
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
    public List<NotificationEntity> fetchAll(UserEntity receiver) {
        List<NotificationEntity> notificationEntities = notificationRepository.fetchAll(receiver.getEmail());
        notificationRepository.saveAll(notificationEntities);
        userService.save(receiver);
        return notificationEntities;
    }

    @Override
    public void removeNotification(UUID uuid, UserEntity user) {
        Optional<NotificationEntity> notificationEntity = notificationRepository.findById(uuid);
        if (notificationEntity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!notificationEntity.get().getReceiver().equals(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        notificationRepository.delete(notificationEntity.get());
        anyNotificationsLeft(user);
    }

    @Override
    public boolean anyNotificationsLeft(UserEntity user) {
        boolean anyLeft = !notificationRepository.fetchAll(user.getEmail()).isEmpty() || !challengeAttemptService.getAttemptsUserCanAllow(user).isEmpty();
        user.setNewNotifications(anyLeft);
        userService.save(user);
        return anyLeft;
    }

}
