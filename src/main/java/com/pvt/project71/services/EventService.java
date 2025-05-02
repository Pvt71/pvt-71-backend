package com.pvt.project71.services;

import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public interface EventService {
    EventEntity save(EventEntity event, UserEntity user);

    List<EventEntity> findAll();

    Optional<EventEntity> findOne(Integer id);

    boolean isExists(Integer id);

    EventEntity partialUpdate(Integer id, EventEntity eventEntity) throws ResponseStatusException;


    void delete(Integer id);

    EventEntity getDefaultEvent();

    EventEntity loadTheLazy(EventEntity toLoad);

    EventEntity addAdmin(EventEntity eventEntity, UserEntity toAdd, UserEntity userAddingThem);

    EventEntity removeAdmin(EventEntity eventEntity, UserEntity toRemove);

}
