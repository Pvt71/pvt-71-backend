package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.BadgeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.ScoreService;
import com.pvt.project71.services.security.JwtService;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private EventService eventService;
    @Autowired
    private ScoreService scoreService;

    @AfterEach
    public void cleanup() {
        userRepository.deleteAll();
    }

    private Jwt getUserToken(UserEntity userEntity){
        return jwtService.generateTokenFromUserEntity(userEntity, 1, ChronoUnit.MINUTES);
    }

    private Jwt getExpiredUserToken(UserEntity userEntity){
        return jwtService.generateTokenFromUserEntity(userEntity, 1,ChronoUnit.NANOS);
    }

    @Test
    public void testCreateUserHttpResponse201IfValidUser() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();

        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
        );
    }

    @Test
    public void testCreateUserHttpResponse404ifNoJWTtoken() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(
                MockMvcResultMatchers.status().isUnauthorized()
        );
    }

    @Test
    public void testCreateUserHttpResponse404ifExpiredJWTtoken() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        Jwt expiredToken = getExpiredUserToken(testUser);

        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(expiredToken))
        ).andExpect(
                MockMvcResultMatchers.status().isUnauthorized()
        );
    }

    @Test
    public void testCreateUserHttpResponse400BadRequestIfBlankEmail() throws Exception {
        UserEntity testUser = TestDataUtil.createInvalidTestUserEntity();

        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(
                MockMvcResultMatchers.status().isBadRequest()
        );
    }

    @Test
    public void testCreateUserSuccessfulReturnSavedAuthor() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();

        testUser.setProfilePicture(null);

        String userJson = objectMapper.writeValueAsString(testUser);


        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.email").value("Test@test.com")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.username").value("TestName")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.school").value("TestSchool")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.profilePictureUrl").doesNotExist()
        );
    }

    @Test
    public void testCreateUserWithoutJWTSuccessfulReturnSavedAuthor() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        testUser.setProfilePicture(null);
        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/public/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.email").value("Test@test.com")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.username").value("TestName")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.school").value("TestSchool")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.profilePictureUrl").doesNotExist()
        );
    }

    @Test
    public void testCreateUserWithoutJWTHttpResponse201IfValidUser() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();

        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/public/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
        );
    }

    @Test
    public void testListUsersSuccessfulHttpResponse200() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        Jwt userToken = getUserToken(testUser);
        userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testListUsersReturnsListOfUsers() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        Jwt userToken = getUserToken(testUser);
        userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(userToken))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].email").value("Test@test.com")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].username").value("TestName")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].school").value("TestSchool")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].profilePictureUrl").value("/uploads/users/Test@test.com/profilePicture")
        );
    }

    @Test
    public void testGetUserHttpResponse200IfUserExists() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/Test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testGetUserReturnsUserIfUserExists() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/" + testUser.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.email").value("Test@test.com")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.username").value("TestName")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.school").value("TestSchool")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.profilePictureUrl").value("/uploads/users/Test@test.com/profilePicture")
        );
    }

    @Test
    public void testGetUserHttpResponse404IfUserDoesNotExists() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/Nonexisting@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }

    @Test
    public void testFullUserUpdateHttpResponse404IfUserDoesNotExist() throws Exception {
        UserEntity userForValidToken = TestDataUtil.createValidTestUserEntity();

        UserDto testUser = TestDataUtil.createValidTestUserDtoB();
        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(getUserToken(userForValidToken)))
        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }

    @Test
    public void testFullUserUpdateHttpResponse404IfNoJWTtoken() throws Exception {
        UserDto testUser = TestDataUtil.createValidTestUserDtoB();
        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(
                MockMvcResultMatchers.status().isUnauthorized()
        );
    }

    @Test
    public void testFullUserUpdateHttpResponse404IfExpiredJWTtoken() throws Exception {
        UserEntity testForExpiredToken = TestDataUtil.createValidTestUserEntity();
        Jwt expiredToken = getExpiredUserToken(testForExpiredToken);

        UserDto testUser = TestDataUtil.createValidTestUserDtoB();
        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(expiredToken))
        ).andExpect(
                MockMvcResultMatchers.status().isUnauthorized()
        );
    }

    @Test
    public void testFullUserUpdateHttpResponse400IfBlankEmailRequestBody() throws Exception {
        UserEntity testUser  = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        UserDto testUserDto = TestDataUtil.createTestUserDtoBlankEmail();
        String userJson = objectMapper.writeValueAsString(testUserDto);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(
                MockMvcResultMatchers.status().isBadRequest()
        );
    }

    @Test
    public void testFullUserUpdateHttpResponse200IfUserExist() throws Exception {
        UserEntity testUser  = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        UserDto testUserDto = TestDataUtil.createValidTestUserDtoA();
        String userJson = objectMapper.writeValueAsString(testUserDto);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testFullUserUpdateSuccessfullyUpdatesUser() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        savedUser.setProfilePicture(TestDataUtil.createTestImageBytes());
        userService.save(savedUser);

        UserDto testUserDto = TestDataUtil.createValidTestUserDtoB();
        testUserDto.setEmail(savedUser.getEmail());
        String userJson = objectMapper.writeValueAsString(testUserDto);


        mockMvc.perform(
                MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.email").value(savedUser.getEmail())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.username").value(testUserDto.getUsername())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.school").value(testUserDto.getSchool())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.profilePictureUrl").value(testUserDto.getProfilePictureUrl())
        );
    }

    @Test
    public void testPartialUpdateReturnHttp404ifNoJWTtoken() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        UserDto testUserDto = TestDataUtil.createValidTestUserDtoA();
        testUserDto.setUsername("UPDATED");
        String userJson = objectMapper.writeValueAsString(testUserDto);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(
                MockMvcResultMatchers.status().isUnauthorized()
        );
    }

    @Test
    public void testPartialUpdateReturnHttp404ifExpiredJWTtoken() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);
        Jwt expiredToken = getExpiredUserToken(testUser);

        UserDto testUserDto = TestDataUtil.createValidTestUserDtoA();
        testUserDto.setUsername("UPDATED");
        String userJson = objectMapper.writeValueAsString(testUserDto);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(expiredToken))
        ).andExpect(
                MockMvcResultMatchers.status().isUnauthorized()
        );
    }


    @Test
    public void testPartialUpdateReturnHttp200IfUserExist() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        UserDto testUserDto = TestDataUtil.createValidTestUserDtoA();
        testUserDto.setUsername("UPDATED");
        String userJson = objectMapper.writeValueAsString(testUserDto);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testPartialUpdateSuccessfullyUpdatesUser() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        UserDto testUserDto = TestDataUtil.createValidTestUserDtoA();
        testUserDto.setUsername("UPDATED");
        String userJson = objectMapper.writeValueAsString(testUserDto);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.email").value(savedUser.getEmail())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.username").value("UPDATED")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.school").value(testUserDto.getSchool())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.profilePictureUrl").value(testUserDto.getProfilePictureUrl())
        );
    }

    @Test
    public void testDeleteUserReturnsHttpStatus204NonExistingUser() throws Exception{
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testDeleteUserReturnsHttpStatus204ExistingUser() throws Exception{
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken(testUser)))
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testDeleteUserReturnsHttpStatus404IfNoJWTtoken() throws Exception{
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/users")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testDeleteUserReturnsHttpStatus404IfExpiredJWTtoken() throws Exception{
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        Jwt expiredToken = getExpiredUserToken(testUser);

        UserEntity savedUser = userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(expiredToken))
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testUserDtoContainsBadgeImageUrls() throws Exception {
        UserEntity user = TestDataUtil.createValidTestUserEntity();

        BadgeEntity badge = BadgeEntity.builder()
                .rank(3)
                .image(new byte[]{1, 2, 3})
                .build();

        badge.setUser(user);

        user.setBadges(List.of(badge));
        userService.save(user);

        Jwt jwt = jwtService.generateTokenFromUserEntity(user, 5, ChronoUnit.MINUTES);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/" + user.getEmail())
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.badges[0].imageUrl").isString());
    }


    @Test
    public void testGetSchoolsWork() throws Exception {
        UserEntity user = TestDataUtil.createValidTestUserEntity();
        UserEntity user2 = TestDataUtil.createValidTestUserEntityB();
        userRepository.save(user);
        userRepository.save(user2);


        mockMvc.perform(
                MockMvcRequestBuilders.get("/schools")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(jsonPath("$", hasSize(2)));
    }
}
