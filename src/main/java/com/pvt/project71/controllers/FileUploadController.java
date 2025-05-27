package com.pvt.project71.controllers;

import com.pvt.project71.domain.entities.*;
import com.pvt.project71.repositories.BadgeRepository;
import com.pvt.project71.services.ChallengeAttemptService;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.security.JwtService;
import com.pvt.project71.services.UserService;
import com.pvt.project71.util.ImageValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;
import net.coobird.thumbnailator.Thumbnails;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/uploads")
public class FileUploadController {

    private final EventService eventService;

    private final ImageValidator imageValidator;

    private final UserService userService;

    private final JwtService jwtService;

    private final ChallengeAttemptService challengeAttemptService;

    private final BadgeRepository badgeRepository;

    @Autowired
    public FileUploadController(EventService eventService,
                                ImageValidator imageValidator,
                                UserService userService,
                                JwtService jwtService,
                                ChallengeAttemptService challengeAttemptService,
                                BadgeRepository badgeRepository) {

        this.eventService = eventService;
        this.userService = userService;
        this.imageValidator = imageValidator;
        this.jwtService = jwtService;
        this.challengeAttemptService = challengeAttemptService;
        this.badgeRepository = badgeRepository;
    }

    //EVENT BADGES
    @PostMapping("/events/{id}/badge")
    public ResponseEntity<Void> uploadEventBadge(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
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

        ByteArrayOutputStream badgeOut = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(100, 100)
                .outputFormat("JPG")
                .toOutputStream(badgeOut);

        EventEntity event = optionalEvent.get();
        event.setBadgePicture(badgeOut.toByteArray());

        eventService.partialUpdate(event.getId(), event, user.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events/{id}/badge")
    public ResponseEntity<byte[]> getEventBadges(@PathVariable Integer id) {
        Optional<EventEntity> optionalEvent = eventService.findOne(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header("Content-Type", "image/jpeg")
                .body(optionalEvent.get().getBadgePicture());

    }
    //Ny get mapping för via user
    @GetMapping("/badges/{id}")
    public ResponseEntity<byte[]> getBadge(@PathVariable Integer id) {
        Optional<BadgeEntity> badge = badgeRepository.findById(id);
        if (badge.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity
                .ok()
                .header("Content-Type", "image/jpeg")
                .body(badge.get().getImage());
    }

    @DeleteMapping("/events/{id}/badge")
    public ResponseEntity<Void> deleteBadgeFromEvent(
            @PathVariable Integer id) {
        Optional<EventEntity> optionalEvent = eventService.findOne(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EventEntity event = optionalEvent.get();
        event.setBadgePicture(null);

        if (event.getAdminUsers() == null || event.getAdminUsers().isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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

        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        user.get().setProfilePicture(file.getBytes());

        ByteArrayOutputStream thumbOut = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(100, 100)
                .outputFormat("JPG")
                .toOutputStream(thumbOut);

        user.get().setProfilePictureThumbnail(thumbOut.toByteArray());

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

    @GetMapping("/users/{email}/profilePicture/thumb")
    public ResponseEntity<byte[]> getUserProfilePictureThumbnail(@PathVariable String email) {
        Optional<UserEntity> optionalUser = userService.findOne(email);
        if (optionalUser.isEmpty() || optionalUser.get().getProfilePictureThumbnail() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header("Content-Type", "image/jpeg")
                .body(optionalUser.get().getProfilePictureThumbnail());
    }

    @DeleteMapping("/users/{email}/profilePicture")
    public ResponseEntity<Void> deleteUserProfilePicture(@PathVariable String email) {
        Optional<UserEntity> optionalUser = userService.findOne(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserEntity user = optionalUser.get();
        user.setProfilePicture(null);
        user.setProfilePictureThumbnail(null);
        userService.partialUpdate(email, user);

        return ResponseEntity.noContent().build();
    }


    // CHALLENGE ATTEMPT IMAGE UPLOAD/GET

    @PostMapping("/challenges/{id}/submit/image")
    public ResponseEntity<Void> uploadChallengeAttemptImage(
            @PathVariable("id") Integer id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt userToken) throws IOException {

        imageValidator.validate(file);

        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<ChallengeAttemptEntity> challengeAttempt = challengeAttemptService
                .find(ChallengeAttemptId.builder().challengeId(id).userEmail(user.get().getEmail()).build());
        if (challengeAttempt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        challengeAttempt.get().setChallengeImage(file.getBytes());

        ByteArrayOutputStream thumbOut = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(100, 100)
                .outputFormat("JPG")
                .toOutputStream(thumbOut);

        challengeAttempt.get().setChallengeThumbnail(thumbOut.toByteArray());
        challengeAttemptService.save(challengeAttempt.get());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/challenges/{id}/attempts/{email}/image")
    public ResponseEntity<byte[]> getChallengeAttemptImage(@PathVariable String email, @PathVariable Integer id) {

        Optional<ChallengeAttemptEntity> optionalChallengeAttempt = challengeAttemptService.
                find(ChallengeAttemptId.builder().challengeId(id).userEmail(email).build());
        if (optionalChallengeAttempt.isEmpty() || optionalChallengeAttempt.get().getChallengeImage() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header("Content-Type", "image/jpeg")
                .body(optionalChallengeAttempt.get().getChallengeImage());
    }
    @GetMapping("/challenges/{id}/attempts/{email}/image/thumb")
    public ResponseEntity<byte[]> getChallengeAttempt(@PathVariable String email, @PathVariable Integer id) {

        Optional<ChallengeAttemptEntity> optionalChallengeAttempt = challengeAttemptService.
                find(ChallengeAttemptId.builder().challengeId(id).userEmail(email).build());
        if (optionalChallengeAttempt.isEmpty() || optionalChallengeAttempt.get().getChallengeThumbnail() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header("Content-Type", "image/jpeg")
                .body(optionalChallengeAttempt.get().getChallengeThumbnail());
    }
}