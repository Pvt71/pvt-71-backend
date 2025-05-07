package com.pvt.project71.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import com.pvt.project71.domain.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeAttemptDto {
    private ChallengeAttemptId id;

    private Status status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime submittedAt;
    private String content;
}
