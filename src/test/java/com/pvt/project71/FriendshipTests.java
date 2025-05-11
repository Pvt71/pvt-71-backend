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
                MockMvcRequestBuilders.post("/friends/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(receiverJson)
                        .with(jwt().jwt(requesterToken))
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
        );
    }

    @Test
    public void testSendRequestHttpResponse400IfAddingYourself() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        Jwt requesterToken = getUserToken(requester);

        String requesterJson = objectMapper.writeValueAsString(requester);
        userService.save(requester);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/friends/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requesterJson)
                        .with(jwt().jwt(requesterToken))
        ).andExpect(
                MockMvcResultMatchers.status().isBadRequest()
        );
    }

    @Test
    public void testSendRequestHttpResponse404IfAddingNonExistingUser() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        Jwt requesterToken = getUserToken(requester);

        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        String receiverJson = objectMapper.writeValueAsString(receiver);
        userService.save(requester);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/friends/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(receiverJson)
                        .with(jwt().jwt(requesterToken))
        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
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
                MockMvcRequestBuilders.post("/friends/add")
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

        FriendshipEntity pendingFriendship = TestDataUtil.createTestPendingFriendshipEntityA();
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
    public void testAcceptFriendRequestHttpResponse400WhenAcceptingYourOwnRequest() throws Exception {
        UserEntity userA = TestDataUtil.createValidTestUserEntity();
        UserEntity userB = TestDataUtil.createValidTestUserEntityB();
        userService.save(userA);
        userService.save(userB);
        Jwt userToken = getUserToken(userA);

        FriendshipEntity pendingFriendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendshipRepository.save(pendingFriendship);
        String friendshipJson = objectMapper.writeValueAsString(pendingFriendship);

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/friends/accept")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(friendshipJson)
                                .with(jwt().jwt(userToken)))
                .andExpect(
                        MockMvcResultMatchers.status().isBadRequest()
                );
    }

    @Test
    public void testAcceptFriendRequestHttpResponse400IfFriendshipIsNotPending() throws Exception {
        UserEntity userA = TestDataUtil.createValidTestUserEntity();
        UserEntity userB = TestDataUtil.createValidTestUserEntityB();
        userService.save(userA);
        userService.save(userB);
        Jwt userToken = getUserToken(userB);

        FriendshipEntity friendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendship.setStatus(Status.ACCEPTED);
        String friendshipJson = objectMapper.writeValueAsString(friendship);

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/friends/accept")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(friendshipJson)
                                .with(jwt().jwt(userToken)))
                .andExpect(
                        MockMvcResultMatchers.status().isBadRequest()
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

    @Test
    public void testRejectFriendRequestHttpResponse204WhenRejected() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        userService.save(requester);
        userService.save(receiver);

        FriendshipEntity friendship = FriendshipEntity.builder().
                id(new FriendshipId(requester.getEmail(), receiver.getEmail())).
                requester(requester).receiver(receiver).status(Status.ACCEPTED).build();
        friendshipRepository.save(friendship);
        String friendshipJson = objectMapper.writeValueAsString(friendship);

        Jwt userToken = getUserToken(receiver);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(friendshipJson)
                        .with(jwt().jwt(userToken))
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testGetFriendRequestsHttpsResponse201() throws Exception {
        UserEntity testUserA = TestDataUtil.createValidTestUserEntity();
        UserEntity testUserB = TestDataUtil.createValidTestUserEntityB();
        userService.save(testUserA);
        userService.save(testUserB);
        Jwt userToken = getUserToken(testUserA);

        FriendshipEntity friendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendshipRepository.save(friendship);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/friends/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testGetFriendRequestsReturnsListOfRequests() throws Exception {
        UserEntity testUserA = TestDataUtil.createValidTestUserEntity();
        UserEntity testUserB = TestDataUtil.createValidTestUserEntityB();
        UserEntity testUserC = TestDataUtil.createValidTestUserEntityC();
        UserEntity testUserD = TestDataUtil.createValidTestUserEntityD();
        userService.save(testUserA);
        userService.save(testUserB);
        userService.save(testUserC);
        userService.save(testUserD);
        Jwt userToken = getUserToken(testUserA);


        FriendshipEntity friendshipA = TestDataUtil.createTestAcceptedFriendshipEntityA();
        FriendshipEntity friendshipB = TestDataUtil.createTestPendingFriendshipEntityA();
        FriendshipEntity friendshipC = TestDataUtil.createTestPendingFriendshipEntityB();
        friendshipRepository.save(friendshipA);
        friendshipRepository.save(friendshipB);
        friendshipRepository.save(friendshipC);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/friends/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.length()").value(2)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].receiver.email").value("TestB@test.com")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].status").value(Status.PENDING.toString())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[1].receiver.email").value("TestC@test.com")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[1].status").value(Status.PENDING.toString())
        );
    }

    @Test
    public void testGetFriendsReturnsHttpsResponse201() throws Exception {
        UserEntity testUserA = TestDataUtil.createValidTestUserEntity();
        UserEntity testUserD = TestDataUtil.createValidTestUserEntityD();
        userService.save(testUserA);
        userService.save(testUserD);
        Jwt userToken = getUserToken(testUserA);


        FriendshipEntity friendshipA = TestDataUtil.createTestAcceptedFriendshipEntityA();
        friendshipRepository.save(friendshipA);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testGetFriendsReturnsListOfAcceptedFriends() throws Exception {
        UserEntity testUserA = TestDataUtil.createValidTestUserEntity();
        UserEntity testUserB = TestDataUtil.createValidTestUserEntityB();
        UserEntity testUserD = TestDataUtil.createValidTestUserEntityD();
        userService.save(testUserA);
        userService.save(testUserB);
        userService.save(testUserD);
        Jwt userToken = getUserToken(testUserA);


        FriendshipEntity friendshipA = TestDataUtil.createTestAcceptedFriendshipEntityA();
        FriendshipEntity friendshipB = TestDataUtil.createTestPendingFriendshipEntityA();
        friendshipRepository.save(friendshipA);
        friendshipRepository.save(friendshipB);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.length()").value(1)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].receiver.email").value("TestD@test.com")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].status").value(Status.ACCEPTED.toString())
        );
    }

    @Test
    public void testDeleteFriendRequestHttpsResponse204() throws Exception {
        UserEntity userA = TestDataUtil.createValidTestUserEntity();
        UserEntity userB = TestDataUtil.createValidTestUserEntityB();
        userService.save(userA);
        userService.save(userB);
        Jwt userToken = getUserToken(userB);

        FriendshipEntity friendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendshipRepository.save(friendship);
        String friendshipJson = objectMapper.writeValueAsString(friendship);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(friendshipJson)
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.status().isNoContent()
        );
    }

    @Test
    public void testDeleteFriendRequestSuccessFullyDeletesRequest() throws Exception {
        UserEntity userA = TestDataUtil.createValidTestUserEntity();
        UserEntity userB = TestDataUtil.createValidTestUserEntityB();
        userService.save(userA);
        userService.save(userB);
        Jwt userToken = getUserToken(userB);

        FriendshipEntity friendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendshipRepository.save(friendship);
        String friendshipJson = objectMapper.writeValueAsString(friendship);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(friendshipJson)
                        .with(jwt().jwt(userToken))
        );
        mockMvc.perform(
                MockMvcRequestBuilders.get("/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.length()").value(0)
        );
    }

}
