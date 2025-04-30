package com.pvt.project71.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    private EventDto event;
    //private UserDto creator;
    @NonNull
    @Positive
    private Integer rewardPoints;
    //private EventDto event;
    private UserDto creator;

    private String description;
}
