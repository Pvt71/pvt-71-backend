package com.pvt.project71.services;

import com.pvt.project71.domain.entities.EventEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface EventService {
    EventEntity save(EventEntity event);

    List<EventEntity> findAll();

    Optional<EventEntity> findOne(Long id);

    boolean isExists(Long id);

    EventEntity partialUpdate(Long id, EventEntity eventEntity);


    void delete(Long id);
}
