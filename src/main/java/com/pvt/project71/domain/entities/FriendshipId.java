package com.pvt.project71.domain.entities;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@Builder
@Embeddable
public class FriendshipId implements Serializable {

    private String requesterEmail;
    private String receiverEmail;

    public FriendshipId(String requesterEmail, String receiverEmail) {
        this.requesterEmail = requesterEmail;
        this.receiverEmail = receiverEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendshipId)) return false;
        FriendshipId that = (FriendshipId) o;
        return Objects.equals(requesterEmail, that.requesterEmail) &&
                Objects.equals(receiverEmail, that.receiverEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requesterEmail, receiverEmail);
    }
}
