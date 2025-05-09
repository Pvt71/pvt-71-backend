package com.pvt.project71.mappers.mapperimpl;

import com.pvt.project71.domain.dto.FriendshipDto;
import com.pvt.project71.domain.entities.FriendshipEntity;
import com.pvt.project71.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class FriendshipMapper implements Mapper<FriendshipEntity, FriendshipDto> {

    private ModelMapper modelMapper;

    public FriendshipMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public FriendshipDto mapTo(FriendshipEntity friendshipEntity) {
        return modelMapper.map(friendshipEntity, FriendshipDto.class);
    }

    @Override
    public FriendshipEntity mapFrom(FriendshipDto friendshipDto) {
        return modelMapper.map(friendshipDto, FriendshipEntity.class);
    }
}
