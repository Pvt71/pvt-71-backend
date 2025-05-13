package com.pvt.project71.mappers.mapperimpl;

import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements Mapper<UserEntity, UserDto> {

    private ModelMapper modelMapper;

    @Autowired
    public UserMapperImpl(ModelMapper modelMapper){
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDto mapTo(UserEntity userEntity) {
        UserDto dto = modelMapper.map(userEntity, UserDto.class);

        if (userEntity.getProfilePicture() != null) {
            dto.setProfilePictureUrl("/uploads/users/" + userEntity.getEmail() + "/profilePicture");
        }

        return dto;
    }

    @Override
    public UserEntity mapFrom(UserDto userDto) {
        UserEntity entity = modelMapper.map(userDto, UserEntity.class);

        // Do not map profilePictureUrl
        entity.setProfilePicture(null);

        return entity;
    }
}
