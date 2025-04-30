package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.repositories.EventRepository;
import com.pvt.project71.services.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class EventServiceImpl implements EventService {

    private EventRepository eventRepository;

    private static final Duration MIN_DURATION_HOURS = Duration.ofHours(24);
    private static final Duration MAX_DURATION_DAYS = Duration.ofDays(365);

    public EventServiceImpl (EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public EventEntity save(EventEntity eventEntity) {
        getDefaultEvent();//Ser till att default alltid finns först som event med id 1.
        if (eventEntity.getChallenges() == null) {
            eventEntity.setChallenges(new ArrayList<>());
        }
        if (eventEntity.getId() == 1 || checkValidDate(eventEntity)){ //Default event ignoreras att göra date check
            return eventRepository.save(eventEntity);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event date is not valid!");
        }
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
    public Optional<EventEntity> findOne(Integer id) {
        return eventRepository.findById(id);
    }

    @Override
    public boolean isExists(Integer id) {
        return eventRepository.existsById(id);
    }

    @Override
    public EventEntity partialUpdate(Integer id, EventEntity eventEntity) throws ResponseStatusException {
        eventEntity.setId(id);
        if (eventEntity.getEndDate() != null && !checkValidDate(eventEntity)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event date is not valid!");
        }
        return eventRepository.findById(id).map(existingEvent -> {
            Optional.ofNullable(eventEntity.getName()).ifPresent(existingEvent::setName);
            Optional.ofNullable(eventEntity.getEndDate()).ifPresent(existingEvent::setEndDate);
            Optional.ofNullable(eventEntity.getDescription()).ifPresent(existingEvent::setDescription);
            return eventRepository.save(existingEvent);
        }).orElseThrow(() -> new RuntimeException("Event does not exist!"));
    }

    @Override
    public void delete(Integer id) {
        eventRepository.deleteById(id);
    }

    @Override
    @Transactional
    public EventEntity getDefaultEvent() {
        Optional<EventEntity> defaultEvent = findOne(1);
        if (defaultEvent.isEmpty()) {
            return eventRepository.save(EventEntity.builder().name("Default").challenges(new ArrayList<>()).build());
        }
        return defaultEvent.get();
    }

    private boolean checkValidDate(EventEntity eventEntity) {
        return eventEntity.getEndDate().isAfter(LocalDateTime.now().plus(MIN_DURATION_HOURS))
                && eventEntity.getEndDate().isBefore(LocalDateTime.now().plus(MAX_DURATION_DAYS));
    }
}
