package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.entities.FriendshipEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.repositories.FriendshipRepository;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.FriendshipService;
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

import java.time.LocalDate;
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
    @Autowired
    private FriendshipService friendshipService;

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
        userService.save(requester);
        userService.save(receiver);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/friends/add/" + receiver.getEmail())
                        .with(jwt().jwt(requesterToken))
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
        );
    }

    @Test
    public void testSendRequestHttpResponse400IfAddingYourself() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        Jwt requesterToken = getUserToken(requester);
        userService.save(requester);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/friends/add/" + requester.getEmail())
                        .with(jwt().jwt(requesterToken))
        ).andExpect(
                MockMvcResultMatchers.status().isBadRequest()
        );
    }

    @Test
    public void testSendRequestHttpResponse400IfFriendshipAlreadyExists() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        UserEntity receiver = TestDataUtil.createValidTestUserEntityD();
        Jwt requesterToken = getUserToken(requester);

        userService.save(requester);
        userService.save(receiver);

        FriendshipEntity friendship = TestDataUtil.createTestAcceptedFriendshipEntityA();
        friendshipService.save(friendship);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/friends/add/" + receiver.getEmail())
                        .with(jwt().jwt(requesterToken))
        ).andExpect(
                MockMvcResultMatchers.status().isBadRequest()
        );

        mockMvc.perform(
                MockMvcRequestBuilders.post("/friends/add/" + requester.getEmail())
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
        userService.save(requester);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/friends/add/" + receiver.getEmail())
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
        userService.save(requester);
        userService.save(receiver);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/friends/add/" + receiver.getEmail())
                        .with(jwt().jwt(requesterToken))
        ).andExpect(jsonPath("$.requester.email").value(requester.getEmail())
        ).andExpect(jsonPath("$.receiver.email").value(receiver.getEmail())
        ).andExpect(jsonPath("$.status").value(Status.PENDING.toString()));
    }

    @Test
    public void testAcceptFriendRequestHttpResponse201WhenAccepted() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        userService.save(requester);
        userService.save(receiver);
        Jwt userToken = getUserToken(receiver);

        FriendshipEntity pendingFriendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendshipRepository.save(pendingFriendship);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/friends/accept/" + receiver.getEmail())
                        .with(jwt().jwt(userToken)))
                .andExpect(
                        MockMvcResultMatchers.status().isOk()
                );
    }

    @Test
    public void testAcceptFriendRequestHttpResponse400WhenAcceptingYourOwnRequest() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        userService.save(requester);
        userService.save(receiver);
        Jwt userToken = getUserToken(requester);

        FriendshipEntity pendingFriendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendshipRepository.save(pendingFriendship);

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/friends/accept/" + receiver.getEmail())
                                .with(jwt().jwt(userToken)))
                .andExpect(
                        MockMvcResultMatchers.status().isBadRequest()
                );
    }

    @Test
    public void testAcceptFriendRequestHttpResponse404IfFriendshipIsNotPending() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        userService.save(requester);
        userService.save(receiver);
        Jwt userToken = getUserToken(receiver);

        FriendshipEntity friendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendship.setStatus(Status.ACCEPTED);

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/friends/accept/" + requester.getEmail())
                                .with(jwt().jwt(userToken)))
                .andExpect(
                        MockMvcResultMatchers.status().isNotFound()
                );
    }

    @Test
    public void testAcceptFriendRequestSuccessfullyChangesStatus() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        userService.save(requester);
        userService.save(receiver);
        Jwt userToken = getUserToken(receiver);

        FriendshipEntity pendingFriendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendshipRepository.save(pendingFriendship);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/friends/accept/" + receiver.getEmail())
                        .with(jwt().jwt(userToken))
        ).andExpect(jsonPath("$.requester.email").value(requester.getEmail())
        ).andExpect(jsonPath("$.receiver.email").value(receiver.getEmail())
        ).andExpect(jsonPath("$.status").value(Status.ACCEPTED.toString())
        ).andExpect(jsonPath("$.friendsSince").value(LocalDate.now().toString())
        );
    }

    @Test
    public void testRejectFriendRequestHttpResponse204WhenRejected() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        userService.save(requester);
        userService.save(receiver);

        FriendshipEntity friendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendshipService.save(friendship);

        Jwt userToken = getUserToken(receiver);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/friends/" + requester.getEmail())
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
                MockMvcRequestBuilders.get("/friendrequests")
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
                MockMvcRequestBuilders.get("/friendrequests")
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.length()").value(2)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].email").value("TestB@test.com")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[1].email").value("TestC@test.com")
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
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.length()").value(1)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].email").value("TestD@test.com")
        );
    }

    @Test
    public void testDeleteFriendHttpsResponse204() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        userService.save(requester);
        userService.save(receiver);
        Jwt userToken = getUserToken(receiver);

        FriendshipEntity friendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendshipRepository.save(friendship);
        String friendshipJson = objectMapper.writeValueAsString(friendship);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/friends/" + requester.getEmail())
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.status().isNoContent()
        );
    }

    @Test
    public void testDeleteFriendSuccessFullyDeletesFriendship() throws Exception {
        UserEntity requester = TestDataUtil.createValidTestUserEntity();
        UserEntity receiver = TestDataUtil.createValidTestUserEntityB();
        userService.save(requester);
        userService.save(receiver);
        Jwt userToken = getUserToken(receiver);

        FriendshipEntity friendship = TestDataUtil.createTestPendingFriendshipEntityA();
        friendshipRepository.save(friendship);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/friends/" + requester.getEmail())
                        .with(jwt().jwt(userToken))
        );
        mockMvc.perform(
                MockMvcRequestBuilders.get("/friends")
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.length()").value(0)
        );
    }

}
