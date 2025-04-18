package com.pvt.project71.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserDto {

    private String email;

    private String username;

    private String school;

    private String profilePictureUrl;

    //När events/scores/challenges finns:
    //private List<Event> events;
    //private List<Score> scores;
    //private List<Challenge> challenges;

}
