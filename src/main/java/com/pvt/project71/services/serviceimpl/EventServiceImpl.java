package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.repositories.EventRepository;
import com.pvt.project71.services.EventService;
import org.springframework.stereotype.Service;

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


}
