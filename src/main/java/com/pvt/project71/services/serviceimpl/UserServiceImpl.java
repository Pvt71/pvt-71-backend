package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserServiceImpl implements UserService {

    //Contains the database functionality
    private UserRepository userRepository;

    private ChallengeRepository challengeRepository;

    public UserServiceImpl(UserRepository userRepository, ChallengeRepository challengeRepository) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
    }

    //CRUD - Create & Update (full)
    @Override
    public UserEntity save(UserEntity user) {
        if(user.getEmail() == null || user.getEmail().isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");

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
        if(email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");

        userEntity.setEmail(email);

        return userRepository.findById(email).map(existingUser -> {
            Optional.ofNullable(userEntity.getUsername()).ifPresent(existingUser::setUsername);
            Optional.ofNullable(userEntity.getProfilePictureUrl()).ifPresent(existingUser::setProfilePictureUrl);
            Optional.ofNullable(userEntity.getSchool()).ifPresent(existingUser::setSchool);

            return userRepository.save(existingUser);
        }).orElseThrow(() -> new RuntimeException("User does not exist"));
    }

    // CRUD - Delete
    @Override
    public void delete(String email) {
        if(email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");

        List<ChallengeEntity> challenges = challengeRepository.findByCreatorEmail(email);

        for(ChallengeEntity challengeEntity : challenges){
            challengeEntity.setCreator(null);
            challengeRepository.save(challengeEntity);
        }

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
        return toReturn;
    }
}
