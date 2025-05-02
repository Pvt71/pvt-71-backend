package com.pvt.project71.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.TimeStamps;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.UserEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventDto {

    private Integer id;

    private String name;

    private String bannerUrl;

    private String description;

    private TimeStamps dates;

    private List<UserDto> adminUsers;


}
