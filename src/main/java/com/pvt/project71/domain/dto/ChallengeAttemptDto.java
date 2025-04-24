package com.pvt.project71.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    Integer challengeId;
    String userEmail;

    Status status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    LocalDateTime submittedAt;
    String content;
}
