package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.FriendshipEntity;
import com.pvt.project71.domain.entities.FriendshipId;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.mappers.mapperimpl.UserMapperImpl;
import com.pvt.project71.repositories.FriendshipRepository;
import com.pvt.project71.services.FriendshipService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    private FriendshipRepository friendshipRepository;
    private UserMapperImpl userMapper;

    public FriendshipServiceImpl(FriendshipRepository friendshipRepository, UserMapperImpl userMapper) {
        this.friendshipRepository = friendshipRepository;
        this.userMapper = userMapper;
    }

    @Override
    public FriendshipEntity save(FriendshipEntity friendshipEntity) {
        if(friendshipEntity == null){
            throw new IllegalArgumentException("Argument cannot be null.");
        }

        return friendshipRepository.save(friendshipEntity);
    }

    @Override
    public List<UserDto> findAllByStatus(String email, Status status) {
        List<UserEntity> friends = new ArrayList<>();

        friends.addAll(friendshipRepository.findRequesterByReceiverEmailAndStatus(email, status));
        friends.addAll(friendshipRepository.findReceiverByRequesterEmailAndStatus(email, status));

        return friends.stream()
                .map(userMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public FriendshipEntity findSpecificFriendRequest(String email) {
        List<FriendshipEntity> friendRequest = friendshipRepository.findAllByReceiverEmailAndStatus(email, Status.PENDING);

        if(friendRequest.isEmpty()){
            return null;
        }
        // Should only exist 1 friend request from one user
        return friendRequest.get(0);
    }

    @Override
    public FriendshipEntity findFriendship(String email1, String email2){
        List<FriendshipEntity> friendship = friendshipRepository.findByEmails(email1, email2);

        if(friendship.isEmpty()){
            friendship = friendshipRepository.findByEmails(email2, email1);

            return (friendship.isEmpty()) ? null : friendship.get(0);
        }

        // Should only exist 1 friendship between two emails
        return friendship.get(0);
    }

    @Override
    public Optional<FriendshipEntity> findOne(FriendshipId id) {
        return friendshipRepository.findById(id);
    }

    @Override
    public void delete(FriendshipId id) {
        friendshipRepository.deleteById(id);
    }

    @Override
    public boolean isExists(String email1, String email2) {
        return !friendshipRepository.findByEmails(email1, email2).isEmpty();
    }

    @Override
    public boolean isExists(FriendshipId friendshipId) {
        return friendshipRepository.existsById(friendshipId);
    }
}
