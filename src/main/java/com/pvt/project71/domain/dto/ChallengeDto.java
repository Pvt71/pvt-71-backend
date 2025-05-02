package com.pvt.project71.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.TimeStamps;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeDto {
    private Integer id;
    @NotBlank
    private String name;
    private TimeStamps dates;

    private EventDto event;
    @NonNull
    @Positive
    private Integer rewardPoints;
    private UserDto creator;

    private String description;
}
