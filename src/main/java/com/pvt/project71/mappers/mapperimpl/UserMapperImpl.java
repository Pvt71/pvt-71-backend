package com.pvt.project71.mappers.mapperimpl;

import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements Mapper<UserEntity, UserDto> {

    private final ModelMapper modelMapper;
    private final BadgeMapper badgeMapper;

    @Autowired
    public UserMapperImpl(ModelMapper modelMapper, BadgeMapper badgeMapper) {
        this.modelMapper = modelMapper;
        this.badgeMapper = badgeMapper;
    }

    @Override
    public UserDto mapTo(UserEntity userEntity) {
        UserDto dto = modelMapper.map(userEntity, UserDto.class);

        if (userEntity.getProfilePicture() != null) {
            dto.setProfilePictureUrl("/uploads/users/" + userEntity.getEmail() + "/profilePicture");
        }

        if (userEntity.getBadges() != null) {
            dto.setBadges(userEntity.getBadges().stream()
                    .map(badgeMapper::mapTo)
                    .toList());
        }

        return dto;
    }

    @Override
    public UserEntity mapFrom(UserDto userDto) {
        UserEntity entity = modelMapper.map(userDto, UserEntity.class);

        entity.setProfilePicture(null);
        entity.setBadges(null);

        return entity;
    }
}
