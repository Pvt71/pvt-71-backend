package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWT;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.UserEntity;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.temporal.ChronoUnit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

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
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        String userJson = objectMapper.writeValueAsString(testUser);
        Jwt userToken = getUserToken(testUser);

        UserEntity testUserB = TestDataUtil.createValidTestUserEntityB();
        userService.save(testUser);
        userService.save(testUserB);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/friends/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
        );
    }

}
