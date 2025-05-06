package com.pvt.project71.domain.entities.score;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "scores")
public class ScoreEntity {
    @EmbeddedId
    private ScoreId scoreId;

    private int score;
}
