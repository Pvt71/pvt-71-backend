package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.EventEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends CrudRepository<EventEntity, Integer>,
        PagingAndSortingRepository<EventEntity, Integer> {
    List<EventEntity> findBySchool(String school);
    Optional<EventEntity> findByName(String name);
}
