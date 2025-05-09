package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.FriendshipEntity;
import com.pvt.project71.domain.entities.FriendshipId;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.repositories.FriendshipRepository;
import com.pvt.project71.services.FriendshipService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    private FriendshipRepository friendshipRepository;

    public FriendshipServiceImpl(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

    @Override
    public FriendshipEntity save(FriendshipEntity friendshipEntity) {
        if(friendshipEntity == null){
            throw new IllegalArgumentException("Argument cannot be null.");
        }

        return friendshipRepository.save(friendshipEntity);
    }

    @Override
    public List<FriendshipEntity> findAll(String email) {
        List<FriendshipEntity> friends = new ArrayList<>();

        friends.addAll(friendshipRepository.findAllByReceiverEmail(email));
        friends.addAll(friendshipRepository.findAllByRequesterEmail(email));

        return friends;
    }

    @Override
    public List<FriendshipEntity> findAllByStatus(String email, Status status) {
        List<FriendshipEntity> friends = new ArrayList<>();

        friends.addAll(friendshipRepository.findAllByReceiverEmailAndStatus(email, status));
        friends.addAll(friendshipRepository.findAllByRequesterEmailAndStatus(email, status));

        return friends;
    }

    @Override
    public Optional<FriendshipEntity> findOne(FriendshipId id) {
        return friendshipRepository.findById(id);
    }

    @Override
    public FriendshipEntity setStatus(FriendshipEntity friendshipEntity, Status status) {
        friendshipEntity.setStatus(status);
        return friendshipRepository.save(friendshipEntity);
    }

    @Override
    public void delete(FriendshipId id) {
        friendshipRepository.deleteById(id);
    }
}
