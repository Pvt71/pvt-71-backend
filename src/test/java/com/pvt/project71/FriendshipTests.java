package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.entities.FriendshipEntity;
import com.pvt.project71.domain.entities.FriendshipId;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.repositories.FriendshipRepository;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.temporal.ChronoUnit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class FriendshipTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FriendshipRepository friendshipRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;

    @AfterEach
    public void cleanup() {
        friendshipRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Jwt getUserToken(UserEntity userEntity){
        return jwtService.mockOauth2(userEntity, 1, ChronoUnit.MINUTES);
    }

    private Jwt getExpiredUserToken(UserEntity userEntity){
        return jwtService.mockOauth2(userEntity, 1,ChronoUnit.NANOS);
    }

    @Test
    public void testSendRequestHttpResponse201IfCreatedRequest() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        Jwt requesterToken = getUserToken(requester);

        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        String receiverJson = objectMapper.writeValueAsString(receiver);
        userService.save(requester);
        userService.save(receiver);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/friends/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(receiverJson)
                        .with(jwt().jwt(requesterToken))
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
        );
    }

    @Test
    public void testSendRequestReturnsFriendship() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        Jwt requesterToken = getUserToken(requester);

        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        String receiverJson = objectMapper.writeValueAsString(receiver);
        userService.save(requester);
        userService.save(receiver);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/friends/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(receiverJson)
                        .with(jwt().jwt(requesterToken))
        ).andExpect(jsonPath("$.requester.email").value(requester.getEmail())
        ).andExpect(jsonPath("$.receiver.email").value(receiver.getEmail())
        ).andExpect(jsonPath("$.status").value(Status.PENDING.toString()));
    }

    @Test
    public void testAcceptFriendRequestHttpResponse201WhenAccepted() throws Exception {
        UserEntity userA = TestDataUtil.createValidTestUserEntity();
        UserEntity userB = TestDataUtil.createValidTestUserEntityB();
        userService.save(userA);
        userService.save(userB);

        FriendshipEntity pendingFriendship = FriendshipEntity.builder().
                id(new FriendshipId(userA.getEmail(), userB.getEmail())).
                requester(userA).receiver(userB).status(Status.PENDING).build();
        friendshipRepository.save(pendingFriendship);
        String friendshipJson = objectMapper.writeValueAsString(pendingFriendship);

        Jwt userToken = getUserToken(userB);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/friends/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(friendshipJson)
                        .with(jwt().jwt(userToken)))
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                );
    }

    @Test
    public void testAcceptFriendRequestSuccessfullyChangesStatus() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        userService.save(requester);
        userService.save(receiver);

        FriendshipEntity pendingFriendship = FriendshipEntity.builder().
                id(new FriendshipId(requester.getEmail(), receiver.getEmail())).
                requester(requester).receiver(receiver).status(Status.PENDING).build();
        friendshipRepository.save(pendingFriendship);
        String friendshipJson = objectMapper.writeValueAsString(pendingFriendship);

        Jwt userToken = getUserToken(receiver);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/friends/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(friendshipJson)
                        .with(jwt().jwt(userToken))
        ).andExpect(jsonPath("$.requester.email").value(requester.getEmail())
        ).andExpect(jsonPath("$.receiver.email").value(receiver.getEmail())
        ).andExpect(jsonPath("$.status").value(Status.ACCEPTED.toString()));
    }

}
