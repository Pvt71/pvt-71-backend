package com.pvt.project71.domain.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key class for ChallengeAttempt
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class ChallengeAttemptId implements Serializable {
    private Integer challengeId;
    private String userEmail;

}
