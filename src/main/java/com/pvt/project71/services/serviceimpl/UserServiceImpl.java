package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }
}
