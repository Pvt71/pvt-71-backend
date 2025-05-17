package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.FriendshipEntity;
import com.pvt.project71.domain.entities.FriendshipId;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.Status;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends CrudRepository<FriendshipEntity, FriendshipId> {

    // Finds receiver when user is requester
    @Query("SELECT f.receiver FROM FriendshipEntity f WHERE f.requester.email = :email AND f.status = :status")
    List<UserEntity> findReceiverByRequesterEmailAndStatus(String email, Status status);

    // Finds requester when user is receiver
    @Query("SELECT f.requester FROM FriendshipEntity f WHERE f.receiver.email = :email AND f.status = :status")
    List<UserEntity> findRequesterByReceiverEmailAndStatus(String email, Status status);

    // For accepting friend requests
    List<FriendshipEntity> findAllByReceiverEmailAndStatus(String email, Status status);

    //Makes it easier when deleting a user
    void deleteAllByRequesterEmail(String email);
    void deleteAllByReceiverEmail(String email);

    // To see if a friendship exists without needing a friendship id
    @Query("SELECT f FROM FriendshipEntity f WHERE " +
            "(f.requester.email = :email1 AND f.receiver.email = :email2) " +
            "OR (f.requester.email = :email2 AND f.receiver.email = :email1)")
    List<FriendshipEntity> findByEmails(String email1, String email2);
}
