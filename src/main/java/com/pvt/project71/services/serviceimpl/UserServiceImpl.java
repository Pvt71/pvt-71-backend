package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.repositories.EventRepository;
import com.pvt.project71.repositories.FriendshipRepository;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserServiceImpl implements UserService {

    private final EventRepository eventRepository;
    //Contains the database functionality
    private UserRepository userRepository;

    private ChallengeRepository challengeRepository;

    private FriendshipRepository friendshipRepository;

    public UserServiceImpl(UserRepository userRepository, ChallengeRepository challengeRepository, EventRepository eventRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.eventRepository = eventRepository;
        this.friendshipRepository = friendshipRepository;
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
        }

        return userRepository.save(user);
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
    @Transactional
    public UserEntity loadTheLazy(UserEntity user) {
        UserEntity toReturn = userRepository.findById(user.getEmail()).get();
        toReturn.getChallenges().isEmpty();
        toReturn.getEvents().isEmpty();
        return toReturn;
    }

    @Override
    @Transactional
    public UserEntity makeAdmin(UserEntity user, EventEntity event) {
        user = loadTheLazy(user);
        if (!user.getEvents().contains(event)) {
            user.getEvents().add(event);
            return userRepository.save(user);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already Admin");
    }

    @Override
    public UserEntity removeAdmin(UserEntity user, EventEntity event) {
        user = loadTheLazy(user);
        if (user.getEvents().contains(event)) {
            user.getEvents().remove(event);
            return userRepository.save(user);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "User was never an Admin");
    }
}
