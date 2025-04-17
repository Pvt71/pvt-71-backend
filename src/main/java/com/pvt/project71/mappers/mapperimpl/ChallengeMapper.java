package com.pvt.project71.mappers.mapperimpl;

import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ChallengeMapper implements Mapper<ChallengeEntity, ChallengeDto> {

    private ModelMapper modelMapper;

    public  ChallengeMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ChallengeDto mapTo(ChallengeEntity challengeEntity) {
        return modelMapper.map(challengeEntity, ChallengeDto.class);
    }

    @Override
    public ChallengeEntity mapFrom(ChallengeDto challengeDto) {
        return modelMapper.map(challengeDto, ChallengeEntity.class);
    }
}
