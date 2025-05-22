package com.pvt.project71.mappers.mapperimpl;

import com.pvt.project71.domain.dto.NotificationDto;
import com.pvt.project71.domain.entities.NotificationEntity;
import com.pvt.project71.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper implements Mapper<NotificationEntity, NotificationDto> {
    private ModelMapper modelMapper;

    public NotificationMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public NotificationDto mapTo(NotificationEntity notificationEntity) {
        return modelMapper.map(notificationEntity, NotificationDto.class);
    }

    @Override
    public NotificationEntity mapFrom(NotificationDto notificationDto) {
        return modelMapper.map(notificationDto, NotificationEntity.class);
    }
}
