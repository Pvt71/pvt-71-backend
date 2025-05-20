package com.pvt.project71.controllers;

import com.pvt.project71.domain.entities.BadgeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.FileStorageService;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import com.pvt.project71.util.ImageValidator;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/uploads")
public class FileUploadController {

    private final EventService eventService;

    private final ImageValidator imageValidator;

    private final UserService userService;

    private final JwtService jwtService;

    @Autowired
    public FileUploadController(EventService eventService,
                                ImageValidator imageValidator,
                                UserService userService,
                                JwtService jwtService) {

        this.eventService = eventService;
        this.userService = userService;
        this.imageValidator = imageValidator;
        this.jwtService = jwtService;
    }

    //EVENT BADGES
    @PostMapping("/events/{id}/badge")
    public ResponseEntity<Void> uploadEventBadge(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String badgeName,
            @AuthenticationPrincipal Jwt userToken) throws IOException {

        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<EventEntity> optionalEvent = eventService.findOne(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        imageValidator.validate(file);

        EventEntity event = optionalEvent.get();
        BadgeEntity badge = BadgeEntity.builder().badgeName(badgeName).image(file.getBytes()).event(event).hasBeenGiven(false).build();

        eventService.partialUpdate(event.getId(), event, user.get());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/events/{id}/badge")
    public ResponseEntity<byte[]> getEventBadges(@PathVariable Integer id) {
        Optional<EventEntity> optionalEvent = eventService.findOne(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BadgeEntity badge = optionalEvent.get().getBadge();

        if (badge == null || badge.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header("Content-Type", "image/jpeg")
                .body(badge.getImage());
    }

    @DeleteMapping("/events/{id}/badge")
    public ResponseEntity<Void> deleteBadgeFromEvent(
            @PathVariable Integer id,
            @PathVariable Long badgeId,
            @AuthenticationPrincipal Jwt userToken) {

        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<EventEntity> optionalEvent = eventService.findOne(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        EventEntity event = optionalEvent.get();

        if(!userService.isAnAdmin(user.get(), event)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        event.setBadge(null);
        eventService.partialUpdate(id, event, event.getAdminUsers().get(0));
        return ResponseEntity.noContent().build();
    }

    //EVENT BANNER UPLOAD/GET
    @PostMapping("/events/{id}/banner")
    public ResponseEntity<Void> uploadEventBanner(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt userToken) throws IOException {
        Optional<EventEntity> optionalEvent = eventService.findOne(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        imageValidator.validate(file);

        EventEntity event = optionalEvent.get();
        event.setBannerImage(file.getBytes());

        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        eventService.partialUpdate(event.getId(), event, user.get());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/events/{id}/banner")
    public ResponseEntity<byte[]> getEventBanner(@PathVariable Integer id) {
        Optional<EventEntity> optionalEvent = eventService.findOne(id);
        if (optionalEvent.isEmpty() || optionalEvent.get().getBannerImage() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header("Content-Type", "image/jpeg")
                .body(optionalEvent.get().getBannerImage());
    }

    @DeleteMapping("/events/{id}/banner")
    public ResponseEntity<Void> deleteEventBanner(@PathVariable Integer id) {
        Optional<EventEntity> optionalEvent = eventService.findOne(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EventEntity event = optionalEvent.get();
        event.setBannerImage(null);

        if (event.getAdminUsers() == null || event.getAdminUsers().isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        eventService.partialUpdate(id, event, event.getAdminUsers().get(0));
        return ResponseEntity.noContent().build();
    }

    //PROFILE PICTURE UPLOAD/GET
    @PostMapping("/users/{email}/profilePicture")
    public ResponseEntity<Void> uploadUserProfilePicture(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt userToken) throws IOException {

        imageValidator.validate(file);

        // TODO : CHECK FOR VALID TOKEN
        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        user.get().setProfilePicture(file.getBytes());

        userService.partialUpdate(user.get().getEmail(), user.get());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{email}/profilePicture")
    public ResponseEntity<byte[]> getUserProfilePicture(@PathVariable String email) {
        Optional<UserEntity> optionalUser = userService.findOne(email);
        if (optionalUser.isEmpty() || optionalUser.get().getProfilePicture() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header("Content-Type", "image/jpeg")
                .body(optionalUser.get().getProfilePicture());
    }

    @DeleteMapping("/users/{email}/profilePicture")
    public ResponseEntity<Void> deleteUserProfilePicture(@PathVariable String email) {
        Optional<UserEntity> optionalUser = userService.findOne(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserEntity user = optionalUser.get();
        user.setProfilePicture(null);
        userService.partialUpdate(email, user);

        return ResponseEntity.noContent().build();
    }
}