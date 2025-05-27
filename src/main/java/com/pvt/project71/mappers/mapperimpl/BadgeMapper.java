package com.pvt.project71.mappers.mapperimpl;


import com.pvt.project71.domain.dto.BadgeDto;
import com.pvt.project71.domain.dto.EventDto;
import com.pvt.project71.domain.entities.BadgeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class BadgeMapper implements Mapper<BadgeEntity, BadgeDto> {

    private ModelMapper modelMapper;

    public BadgeMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public BadgeDto mapTo(BadgeEntity badgeEntity) {
        BadgeDto dto = modelMapper.map(badgeEntity, BadgeDto.class);

        if (badgeEntity.getImage() != null) {
            dto.setImageUrl("/uploads/badges/" + badgeEntity.getId());
        }
        return dto;
    }

    @Override
    public BadgeEntity mapFrom(BadgeDto badgeDto) {
        BadgeEntity entity = modelMapper.map(badgeDto, BadgeEntity.class);

        // Do not map bannerUrl
        entity.setImage(null);
        return entity;
    }
}
