package com.pvt.project71.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreDto {
    @Positive(message = "Score must be a positive integer")
    private int score;
    //Score belongs to
    @NonNull
    private UserDto userDto;
    @NonNull
    private int eventId;
}
