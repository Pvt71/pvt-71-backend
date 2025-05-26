package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.BadgeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.repositories.EventRepository;
import com.pvt.project71.repositories.ScoreRepository;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.NotificationService;
import com.pvt.project71.services.ScheduleService;
import com.pvt.project71.services.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private EventService eventService;

    public ScheduleServiceImpl(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    @Scheduled(fixedRate = 60000)
    public void monitorMinutely() {
        List<EventEntity> events = eventService.findAllExpiredEvents();
        for (EventEntity e : events) {
            eventService.giveBadges(e);
            e.setBadgesGiven(true);
            eventService.save(e, e.getAdminUsers().get(0));
        }
    }

    @Override
    @Scheduled(fixedRate =  86400000)
    public void monitorDaily() {
        eventService.deleteOldOnes();

    }
}
