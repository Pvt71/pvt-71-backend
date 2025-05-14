package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.EventEntity;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
