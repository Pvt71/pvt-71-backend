package com.pvt.project71.domain.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@Table(name = "events")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "events_id_seq")
    private long id;

    private String name;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

//    @ManyToMany(cascade = CascadeType.ALL)
//    @JoinColumn(name = "user_id")
//    private UserEntity userEntity;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
    @JsonIgnore //ignorerar listan när Entity görs till Json
    private List<ChallengeEntity> challenges;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventEntity)) return false;
        EventEntity eventEntity = (EventEntity) o;
        return id == eventEntity.id && Objects.equals(name, eventEntity.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }


}
