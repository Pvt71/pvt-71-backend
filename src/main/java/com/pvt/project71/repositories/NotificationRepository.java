package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.NotificationEntity;
import com.pvt.project71.domain.entities.UserEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends CrudRepository<NotificationEntity, UUID> {
    @Query("""
    SELECT n FROM NotificationEntity n
    WHERE n.receiver.email =:userEmail
    ORDER BY n.receivedAt DESC
    """)
    List<NotificationEntity> fetchAll(String userEmail);

    @Modifying
    @Transactional
    @Query("""
    DELETE FROM NotificationEntity n
    WHERE n.receiver.email =:userEmail
    """)
    void deleteAllRead(String userEmail);
}
