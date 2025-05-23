package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.dto.NotificationDto;
import com.pvt.project71.domain.entities.NotificationEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.mappers.mapperimpl.ChallengeAttemptMapper;
import com.pvt.project71.services.ChallengeAttemptService;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.NotificationService;
import com.pvt.project71.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
public class NotificationController {

    private NotificationService notificationService;
    private JwtService jwtService;
    private UserService userService;
    private Mapper<NotificationEntity, NotificationDto> notificationMapper;
    private ChallengeAttemptService challengeAttemptService;
    private ChallengeAttemptMapper challengeAttemptMapper;

    public NotificationController(NotificationService notificationService, JwtService jwtService, UserService userService, Mapper<NotificationEntity, NotificationDto> notificationMapper, ChallengeAttemptService challengeAttemptService, ChallengeAttemptMapper challengeAttemptMapper) {
        this.notificationService = notificationService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.notificationMapper = notificationMapper;
        this.challengeAttemptService = challengeAttemptService;
        this.challengeAttemptMapper = challengeAttemptMapper;
    }

    @GetMapping("/notifications/anyNew")
    public ResponseEntity checkForNewNotifications(@AuthenticationPrincipal Jwt userToken) {
        UserEntity user = checkAndRetrieveUserFromToken(userToken);

          return user.isNewNotifications() ? new ResponseEntity<>(HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/notifications/fetch")
    public ResponseEntity<List<Object>> fetchUnread(@AuthenticationPrincipal Jwt userToken) {
        UserEntity user = checkAndRetrieveUserFromToken(userToken);
        if (!user.isNewNotifications()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        List<ChallengeAttemptDto> attemptDtos = challengeAttemptService.getAttemptsUserCanAllow(user).stream()
                .map(challengeAttemptMapper::mapTo)
                .collect(toList());
        List<NotificationEntity> notificationEntities = notificationService.fetchUnread(user);
        List<NotificationDto> notificationDtos = notificationEntities.stream().map(notificationMapper::mapTo).toList();
        List<Object> allDtos = new ArrayList<>();
        allDtos.addAll(attemptDtos);
        allDtos.addAll(notificationDtos);
        return new ResponseEntity<>(allDtos, HttpStatus.OK);
    }
    
    @GetMapping("/notifications/retry")
    public ResponseEntity<List<NotificationDto>> fetchAll(@AuthenticationPrincipal Jwt userToken) {
        UserEntity user = checkAndRetrieveUserFromToken(userToken);
        List<NotificationEntity> notificationEntities = notificationService.fetchAll(user);
        List<NotificationDto> dtos = notificationEntities.stream().map(notificationMapper::mapTo).toList();
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @DeleteMapping("/notifications/acknowledge")
    public ResponseEntity deleteAllReadNotifications(@AuthenticationPrincipal Jwt userToken) {
        UserEntity user = checkAndRetrieveUserFromToken(userToken);
        notificationService.deleteAllRead(user);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private UserEntity checkAndRetrieveUserFromToken(Jwt userToken) {
        if (!jwtService.isTokenValid(userToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is not valid");
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token could not be linked to an user");
        }
        return user.get();
    }
}
