package com.pvt.project71.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserDto {

    //@Email checks if email is in the format of something@domain.com
    @Email(message = "Invalid email format.")
    //@NotBlank checks whether email is blank or not
    @NotBlank(message = "Email is required.")
    private String email;

    private String username;

    private String school;

    private String profilePictureUrl;
    //När events/scores/challenges finns:
    //private List<Event> events;
    //private List<Score> scores;
    //private List<Challenge> challenges;

}
