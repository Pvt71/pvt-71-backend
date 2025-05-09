package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.FriendshipEntity;
import com.pvt.project71.domain.entities.FriendshipId;
import com.pvt.project71.domain.enums.Status;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends CrudRepository<FriendshipEntity, FriendshipId> {

    List<FriendshipEntity> findAllByRequesterEmail(String email);
    List<FriendshipEntity> findAllByReceiverEmail(String email);

    List<FriendshipEntity> findAllByRequesterEmailAndStatus(String email, Status status);
    List<FriendshipEntity> findAllByReceiverEmailAndStatus(String email, Status status);
}
