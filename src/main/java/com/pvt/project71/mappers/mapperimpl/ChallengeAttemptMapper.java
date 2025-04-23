package com.pvt.project71.mappers.mapperimpl;

import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import com.pvt.project71.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

@Component
public class ChallengeAttemptMapper implements Mapper<ChallengeAttemptEntity, ChallengeAttemptDto> {

    private ModelMapper modelMapper;

    public  ChallengeAttemptMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;

    }

    @Override
    public ChallengeAttemptDto mapTo(ChallengeAttemptEntity challengeAttemptEntity) {
        return modelMapper.map(challengeAttemptEntity, ChallengeAttemptDto.class);
    }

    @Override
    public ChallengeAttemptEntity mapFrom(ChallengeAttemptDto challengeAttemptDto) {
        return modelMapper.map(challengeAttemptDto, ChallengeAttemptEntity.class);
    }
}
