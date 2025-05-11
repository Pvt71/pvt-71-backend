package com.pvt.project71.domain.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pvt.project71.domain.enums.Status;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="friendships")
public class FriendshipEntity {

    @EmbeddedId
    FriendshipId id;

    @ManyToOne()
    @JoinColumn(name = "requesterEmail", insertable = false, updatable = false)
    private UserEntity requester;

    @ManyToOne()
    @JoinColumn(name = "receiverEmail", insertable = false, updatable = false)
    private UserEntity receiver;

    @Enumerated(EnumType.STRING)
    private Status status;

}
