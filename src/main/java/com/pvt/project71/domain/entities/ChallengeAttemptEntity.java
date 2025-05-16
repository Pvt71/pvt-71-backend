package com.pvt.project71.domain.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="challengeAttempts")
public class ChallengeAttemptEntity {
    @EmbeddedId
    ChallengeAttemptId id;

    private Status status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime submittedAt;
    private String content;
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("challengeId")
    @JoinColumn(name = "challenge_id", insertable=false, updatable=false)
    private ChallengeEntity challenge;

    private boolean isContentHidden;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChallengeAttemptEntity)) return false;
        ChallengeAttemptEntity other = (ChallengeAttemptEntity) o;
        return Objects.equals(other.id, id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
