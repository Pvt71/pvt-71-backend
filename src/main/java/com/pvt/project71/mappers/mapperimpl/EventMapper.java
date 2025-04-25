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
    public EventDto mapTo(EventEntity projectEntity) {
        return modelMapper.map(projectEntity, EventDto.class);
    }

    @Override
    public EventEntity mapFrom(EventDto projectDto) {
        return modelMapper.map(projectDto, EventEntity.class);
    }
}