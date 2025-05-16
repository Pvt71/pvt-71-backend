package com.pvt.project71.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.enums.ProofType;
import com.pvt.project71.domain.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private Integer points;
    private UserDto creator;

    private String description;
    private ProofType proofType;
    private Integer maxCompletions;
    private boolean isSocial;

    private Status status;
}
