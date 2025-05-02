package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserServiceImpl implements UserService {

    //Contains the database functionality
    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    //CRUD - Create & Update (full)
    @Override
    public UserEntity save(UserEntity user) {
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
        return userRepository.findById(email);
    }

    //CRUD - Update (partial)
    @Override
    public UserEntity partialUpdate(String email, UserEntity userEntity) {
        userEntity.setEmail(email);

        return userRepository.findById(email).map(existingUser -> {
            //If attribute exists and not null, update said attribute
            Optional.ofNullable(userEntity.getUsername()).ifPresent(existingUser::setUsername);
            Optional.ofNullable(userEntity.getProfilePictureUrl()).ifPresent(existingUser::setProfilePictureUrl);
            Optional.ofNullable(userEntity.getSchool()).ifPresent(existingUser::setSchool);

            return userRepository.save(existingUser);
        }).orElseThrow(() -> new RuntimeException("User does not exist"));
    }

    // CRUD - Delete
    @Override
    public void delete(String email) {
        userRepository.deleteById(email);
    }

    @Override
    public boolean isExists(String email) {
        return userRepository.existsById(email);
    }

    @Override
    @Transactional
    public UserEntity loadTheLazy(UserEntity user) {
        UserEntity toReturn = userRepository.findById(user.getEmail()).get();
        toReturn.getChallenges().isEmpty();
        return toReturn;
    }

    @Override
    @Transactional
    public UserEntity makeAdmin(UserEntity user, EventEntity event) {
        if (!event.getAdminUsers().contains(user)) {
            user.getEvents().add(event);
        }
        return userRepository.save(user);
    }
}
