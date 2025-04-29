package com.pvt.project71.services;

import com.pvt.project71.domain.entities.UserEntity;

import java.util.List;
import java.util.Optional;

public interface    UserService {

    // CRUD - Create & Update (full)
    UserEntity save(UserEntity user);

    // CRUD - Read (many)
    List<UserEntity> findAll();

    // CRUD - Read (one)
    Optional<UserEntity> findOne(String email);

    // CRUD - Update (partial)
    UserEntity partialUpdate(String email, UserEntity userEntity);

    // CRUD - Delete
    void delete(String email);

    boolean isExists(String email);
}
