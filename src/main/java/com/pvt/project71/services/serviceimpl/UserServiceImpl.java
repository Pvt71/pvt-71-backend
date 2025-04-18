package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserServiceImpl implements UserService {

    //Contains the CRUD functionality
    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    //Create
    @Override
    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }

    //Read (many)
    @Override
    public List<UserEntity> findAll() {
        return StreamSupport.stream(userRepository
                        .findAll()
                        .spliterator(),
                        false)
                .collect(Collectors.toList());
    }

    //Read (one)
    @Override
    public Optional<UserEntity> findOne(String email) {
        return userRepository.findById(email);
    }
}
