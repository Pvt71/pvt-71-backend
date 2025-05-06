package com.pvt.project71.domain.entities.score;

import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ScoreId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "email", nullable = false)
    private UserEntity user;
    @ManyToOne
    @JoinColumn(name = "eventId",nullable = false)
    private EventEntity event;

}
