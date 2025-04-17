package com.pvt.project71.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeDto {
    private Integer id;
    private String name;
    private LocalDateTime endDate;
    //private EventDto event;
    //private UserDto creator;

    private int rewardPoints;
    private String description;
}
