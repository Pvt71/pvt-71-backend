package com.pvt.project71.services;

import com.pvt.project71.domain.entities.UserEntity;

import java.util.List;

public interface UserService {

    UserEntity createUser(UserEntity user);

    List<UserEntity> findAll();
}
