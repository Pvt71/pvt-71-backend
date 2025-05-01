package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.TimeStamps;
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
        } if (eventEntity.getDates().getCreatedAt() == null) {
            eventEntity.getDates().setCreatedAt(LocalDateTime.now());
            eventEntity.getDates().setUpdatedAt(eventEntity.getDates().getCreatedAt());
        }if (eventEntity.getDates().getStartsAt() == null) {
            eventEntity.getDates().setStartsAt(eventEntity.getDates().getCreatedAt());
        }
        if (eventEntity.getId() == null) {
            if (checkValidDate(eventEntity)) {
                return eventRepository.save(eventEntity);
            }
        } else if (eventEntity.getId() == 1 || checkValidDate(eventEntity)){ //Default event ignoreras att göra date check
            return eventRepository.save(eventEntity);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event date is not valid!");
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
        eventEntity.getDates().setUpdatedAt(LocalDateTime.now());
        eventEntity.getDates().setUpdatedAt(LocalDateTime.now());
        return eventRepository.findById(id).map(existingEvent -> {
            Optional.ofNullable(eventEntity.getName()).ifPresent(existingEvent::setName);
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
            return eventRepository.save(EventEntity.builder().name("Default").challenges(new ArrayList<>())
                    .dates(TimeStamps.builder().startsAt(LocalDateTime.now()).createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now()).build()).build());
        }
        return defaultEvent.get();
    }

    /**
     * Laddar in Lazyloaded relationer
     */
    @Override
    @Transactional
    public EventEntity loadTheLazy(EventEntity toLoad) {
        EventEntity eventEntity = eventRepository.findById(toLoad.getId()).get();
        eventEntity.getChallenges().isEmpty();
        return eventEntity;
    }

    private boolean checkValidDate(EventEntity eventEntity) {
        if (!eventEntity.getDates().getCreatedAt().equals(eventEntity.getDates().getUpdatedAt())) {
            return  true;
        }
        return eventEntity.getEndDate().isAfter(LocalDateTime.now().plus(MIN_DURATION_HOURS))
                && eventEntity.getEndDate().isBefore(LocalDateTime.now().plus(MAX_DURATION_DAYS));
    }
}
