package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.repositories.EventRepository;
import com.pvt.project71.services.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class EventServiceImpl implements EventService {

    private EventRepository eventRepository;

    public EventServiceImpl (EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public EventEntity save(EventEntity eventEntity) {
        getDefaultEvent();//Ser till att default alltid finns först som event med id 1.
        if (eventEntity.getChallenges() == null) {
            eventEntity.setChallenges(new ArrayList<>());
        }
        return eventRepository.save(eventEntity);
    }

    @Override
    public List<EventEntity> findAll(){
        return StreamSupport.stream(eventRepository
                        .findAll()
                        .spliterator()
                        , false)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<EventEntity> findOne(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public boolean isExists(Long id) {
        return eventRepository.existsById(id);
    }

    @Override
    public EventEntity partialUpdate(Long id, EventEntity eventEntity) {
        eventEntity.setId(id);

        return eventRepository.findById(id).map(existingEvent -> {
            Optional.ofNullable(eventEntity.getName()).ifPresent(existingEvent::setName);
            Optional.ofNullable(eventEntity.getEndDate()).ifPresent(existingEvent::setEndDate);
            Optional.ofNullable(eventEntity.getDescription()).ifPresent(existingEvent::setDescription);
            return eventRepository.save(existingEvent);
        }).orElseThrow(() -> new RuntimeException("Event does not exist!"));
    }

    @Override
    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    @Override
    @Transactional
    public EventEntity getDefaultEvent() {
        Optional<EventEntity> defaultEvent = findOne(1L);
        if (defaultEvent.isEmpty()) {
            return eventRepository.save(EventEntity.builder().name("Default").challenges(new ArrayList<>()).build());
        }
        return defaultEvent.get();
    }
}
