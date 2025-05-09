package com.pvt.project71.services;

import com.pvt.project71.domain.entities.FriendshipEntity;
import com.pvt.project71.domain.entities.FriendshipId;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.Status;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface FriendshipService {

    FriendshipEntity save(FriendshipEntity friendshipEntity);

    List<FriendshipEntity> findAll(String email);

    List<FriendshipEntity> findAllByStatus(String email, Status status);

    Optional<FriendshipEntity> findOne(FriendshipId id);

    FriendshipEntity setStatus(FriendshipEntity friendshipEntity, Status status);

    void delete(FriendshipId friendshipEntity);
}
