package com.pvt.project71.domain.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.enums.ProofType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="challenges")
public class ChallengeEntity {

    @Id
    @GeneratedValue(generator = "IDENTITY")
    private Integer id;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    private String name;
    private Integer maxCompletions;

    //@ManyToOne
    //@JoinColumn(name = "placeholder", nullable = false)
    //private EventEntity event;

    //@ManyToOne
    //@JoinColumn(name = "creator_email", nullable = false)
    //private UserEntity creator;

    private int rewardPoints;
    private String description;
    private ProofType proofType;

}
