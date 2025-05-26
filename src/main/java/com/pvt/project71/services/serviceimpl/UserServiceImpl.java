package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.repositories.EventRepository;
import com.pvt.project71.repositories.FriendshipRepository;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.ScoreService;
import com.pvt.project71.services.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserServiceImpl implements UserService {

    private final EventRepository eventRepository;
    //Contains the database functionality
    private UserRepository userRepository;

    private ChallengeRepository challengeRepository;

    private FriendshipRepository friendshipRepository;

    private ScoreService scoreService;

    private EventService eventService;

    public UserServiceImpl(UserRepository userRepository,
                           ChallengeRepository challengeRepository,
                           EventRepository eventRepository,
                           FriendshipRepository friendshipRepository,
                           ScoreService scoreService,
                           @Lazy EventService eventService) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.eventRepository = eventRepository;
        this.friendshipRepository = friendshipRepository;
        this.scoreService = scoreService;
        this.eventService = eventService;
    }

    //CRUD - Create & Update (full)
    @Override
    public UserEntity save(UserEntity user) {
        if(user == null){
            throw new IllegalArgumentException("Argument cannot be null.");
        }

        if(user.getEmail() == null || user.getEmail().isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");

        if (user.getEvents() == null) {
            user.setEvents(new ArrayList<>());
        } if (user.getChallenges() == null) {
            user.setChallenges(new ArrayList<>());
        } if (user.getScores() == null) {
            user.setScores(new ArrayList<>());
        } if (user.getBadges() == null) {
            user.setBadges(new ArrayList<>());
        } if (user.getNotifications() == null) {
            user.setNotifications(new ArrayList<>());
        }

        user = userRepository.save(user);
        EventEntity usersDefault = eventService.getDefaultEvent(user.getSchool());
        if (scoreService.findOne(new ScoreId(user, usersDefault)).isEmpty()) {
            scoreService.create(ScoreEntity.builder().scoreId(new ScoreId(user, usersDefault)).build());
        }
        return user;
    }

    //CRUD - Read (many)
    @Override
    @Transactional
    public List<UserEntity> findAll() {
        return StreamSupport.stream(userRepository
                        .findAll()
                        .spliterator(),
                        false)
                .collect(Collectors.toList());
    }

    //CRUD - Read (one)
    @Override
    @Transactional
    public Optional<UserEntity> findOne(String email) {
        if(email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");

        return userRepository.findById(email);
    }

    //CRUD - Update (partial)
    @Override
    public UserEntity partialUpdate(String email, UserEntity userEntity) {
        if(userEntity == null){
            throw new IllegalArgumentException("userEntity cannot be null.");
        }

        if(email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");

        userEntity.setEmail(email);

        return userRepository.findById(email).map(existingUser -> {
            //If attribute exists and not null, update said attribute
            Optional.ofNullable(userEntity.getUsername()).ifPresent(existingUser::setUsername);
            Optional.ofNullable(userEntity.getProfilePicture()).ifPresent(existingUser::setProfilePicture);
            Optional.ofNullable(userEntity.getProfilePictureThumbnail()).ifPresent(existingUser::setProfilePictureThumbnail);
            Optional.ofNullable(userEntity.getSchool()).ifPresent(existingUser::setSchool);

            return userRepository.save(existingUser);
        }).orElseThrow(() -> new RuntimeException("User does not exist"));
    }

    // CRUD - Delete
    @Override
    @Transactional
    public void delete(String email) {
        if(email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");

        // To be able to delete users that are admins in events
        Optional<UserEntity> user = userRepository.findById(email);
        if(user.isPresent()) {
            for (EventEntity event : user.get().getEvents()) {
                if(event.getAdminUsers() != null) {
                    event.getAdminUsers().remove(user.get());
                    eventRepository.save(event);
                }
            }
            user.get().getEvents().clear();
        }

        friendshipRepository.deleteAllByRequesterEmail(email);
        friendshipRepository.deleteAllByReceiverEmail(email);

        userRepository.deleteById(email);
    }

    @Override
    public boolean isExists(String email) {
        if(email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");


        return userRepository.existsById(email);
    }

    @Override
    public List<String> getSchools(){
        return userRepository.findAllSchools();
    }

    @Override
    public Optional<ScoreEntity> getDefaultScore(String email){
        return userRepository.findScoresFromUserSchool(email);
    }

    @Override
    @Transactional
    public UserEntity loadTheLazy(UserEntity user) {
        UserEntity toReturn = userRepository.findById(user.getEmail()).get();
        toReturn.getChallenges().isEmpty();
        toReturn.getEvents().isEmpty();
        toReturn.getScores().isEmpty();
        toReturn.getNotifications().isEmpty();
        return toReturn;
    }

    @Override
    public boolean isAnAdmin(UserEntity userEntity, EventEntity eventEntity) {
        return (eventEntity.getId() != null && eventEntity.isDefault()) || eventEntity.getAdminUsers().contains(userEntity);
    }

    @Override
    @Transactional
    public UserEntity makeAdmin(UserEntity toAdd, EventEntity event, UserEntity userAddingThem) {
        if (!isAnAdmin(userAddingThem, event)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can add new admins");
        } if (event.getAdminUsers().size() > 9) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Max Admins reached");
        } if (event.getAdminUsers().contains(toAdd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already Admin");
        }
        toAdd = loadTheLazy(toAdd);
        if (!toAdd.getEvents().contains(event)) {
            toAdd.getEvents().add(event);
            toAdd = userRepository.save(toAdd);
        }
        event.getAdminUsers().add(toAdd);
        eventRepository.save(event);
        return toAdd;
    }

    @Override
    public UserEntity removeAdmin(UserEntity toRemove, EventEntity event) {
        toRemove = loadTheLazy(toRemove);
        if (!isAnAdmin(toRemove, event)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not an admin"); //Tanken är att bara en själv kallar remove
        }
        if (toRemove.getEvents().contains(event)) {
            toRemove.getEvents().remove(event);
            toRemove = userRepository.save(toRemove);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User was never an Admin");
        }
        event.getAdminUsers().remove(toRemove);
        eventRepository.save(event);
        return toRemove;
    }

    @Override
    public Boolean hasNewNotifications(String userEmail) {
        Optional<UserEntity> user = userRepository.findById(userEmail);
        return user.isPresent() && user.get().isNewNotifications();
    }
}



