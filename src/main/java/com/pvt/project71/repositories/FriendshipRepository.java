package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.FriendshipEntity;
import com.pvt.project71.domain.entities.FriendshipId;
import com.pvt.project71.domain.enums.Status;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends CrudRepository<FriendshipEntity, FriendshipId> {

    List<FriendshipEntity> findAllByRequesterEmailAndStatus(String email, Status status);
    List<FriendshipEntity> findAllByReceiverEmailAndStatus(String email, Status status);

    //Makes it easier when deleting a user
    void deleteAllByRequesterEmail(String email);
    void deleteAllByReceiverEmail(String email);

    @Query("SELECT f FROM FriendshipEntity f WHERE " +
            "(f.requester.email = :email1 AND f.receiver.email = :email2) " +
            "OR (f.requester.email = :email2 AND f.receiver.email = :email1)")
    List<FriendshipEntity> findByEmails(String email1, String email2);
}
