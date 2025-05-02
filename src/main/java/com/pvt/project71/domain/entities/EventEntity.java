package com.pvt.project71.domain.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pvt.project71.domain.TimeStamps;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
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
    @GeneratedValue(generator = "IDENTITY")
    private Integer id;

    @NotBlank
    private String name;

    private String description;
    private String bannerUrl;
    @Embedded
    private TimeStamps dates;

    @ManyToMany(cascade = CascadeType.MERGE,fetch = FetchType.EAGER)
    @JoinTable(
            name = "event_admins", // join table name
            joinColumns = @JoinColumn(name = "event_id"), // owning side
            inverseJoinColumns = @JoinColumn(name = "user_id") // the other side
    )
    private List<UserEntity> adminUsers;


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
