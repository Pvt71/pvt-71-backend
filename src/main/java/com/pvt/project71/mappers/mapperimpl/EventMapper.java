package com.pvt.project71.mappers.mapperimpl;

import com.pvt.project71.domain.dto.EventDto;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class EventMapper implements Mapper<EventEntity, EventDto> {

    private ModelMapper modelMapper;

    public EventMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public EventDto mapTo(EventEntity eventEntity) {
        EventDto dto = modelMapper.map(eventEntity, EventDto.class);

        if (eventEntity.getBannerImage() != null) {
            dto.setBannerUrl("/uploads/events/" + eventEntity.getId() + "/banner");
        }

        if (eventEntity.getBadgePicture() != null) {
            dto.setBadgeUrl("/uploads/events/" + eventEntity.getId() + "/badge");
        }

        return dto;
    }

    @Override
    public EventEntity mapFrom(EventDto eventDto) {

        EventEntity entity = modelMapper.map(eventDto, EventEntity.class);

        // Do not map bannerUrl
        entity.setBannerImage(null);

        entity.setBadgePicture(null);

        return entity;
    }
}