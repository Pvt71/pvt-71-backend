package com.pvt.project71.domain.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChallengeAttemptId other = (ChallengeAttemptId) o;

        return other.getChallengeId().equals(challengeId) && other.getUserEmail().equals(userEmail);
    }


    @Override
    public int hashCode() {
        return Objects.hash(userEmail, challengeId);
    }
}

