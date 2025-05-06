package com.pvt.project71.mappers.mapperimpl;

import com.pvt.project71.domain.dto.ScoreDto;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import com.pvt.project71.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ScoreMapper  implements Mapper<ScoreEntity, ScoreDto> {

    private final ModelMapper   modelMapper;

    public ScoreMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ScoreDto mapTo(ScoreEntity scoreEntity) {
        return modelMapper.map(scoreEntity,ScoreDto.class);
    }

    @Override
    public ScoreEntity mapFrom(ScoreDto scoreDto) {
        return modelMapper.map(scoreDto, ScoreEntity.class);
    }

}
