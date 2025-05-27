package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.EventEntity;
import jakarta.transaction.Transactional;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends CrudRepository<EventEntity, Integer>,
        PagingAndSortingRepository<EventEntity, Integer> {
    @Query("""
    SELECT e FROM EventEntity e
    WHERE e.school = :school
      AND e.isDefault = false
      AND (e.dates.endsAt IS NULL OR e.dates.endsAt > CURRENT_TIMESTAMP)
    ORDER BY e.dates.updatedAt DESC
""")
    List<EventEntity> findBySchool(@Param("school") String school);
    Optional<EventEntity> findByName(String name);

    @Query("""
    SELECT e FROM EventEntity e
    WHERE EXISTS (
    SELECT u FROM e.adminUsers u
    WHERE u.email =:email)
    AND (e.dates.endsAt IS NULL OR e.dates.endsAt > CURRENT_TIMESTAMP)
    ORDER BY e.dates.updatedAt DESC
    """)
    List<EventEntity> findByUserAdmin(String email);

    @Query("""
    SELECT e FROM EventEntity e
    WHERE e.isDefault = false
    AND e.dates.endsAt < CURRENT_TIMESTAMP
    AND e.badgesGiven = false
    """)
    List<EventEntity> findAllExpiredEvents();

    @Modifying
    @Transactional
    @Query("""
    DELETE FROM EventEntity e
    WHERE e.isDefault = false
    AND e.dates.endsAt< :t
    
    """)
    void deleteOldEvents(LocalDateTime t);
}
