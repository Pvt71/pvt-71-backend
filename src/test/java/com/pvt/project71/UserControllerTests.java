package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserControllerTests {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UserService userService;

    @Autowired
    public UserControllerTests(MockMvc mockMvc, UserService userService){
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.userService = userService;
    }

    @Test
    public void testCreateUserHttpResponse201IfValidUser() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();

        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
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
        ).andExpect(
                MockMvcResultMatchers.status().isBadRequest()
        );
    }

    @Test
    public void testCreateUserSuccessfulReturnSavedAuthor() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();

        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.email").value("Test@test.com")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.username").value("TestName")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.school").value("TestSchool")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.profilePictureUrl").value("testUrl")
        );
    }

    @Test
    public void testListUsersSuccessfulHttpResponse200() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testListUsersReturnsListOfUsers() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();

        userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].email").value("Test@test.com")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].username").value("TestName")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].school").value("TestSchool")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].profilePictureUrl").value("testUrl")
        );
    }

    @Test
    public void testGetUserHttpResponse200IfUserExists() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/Test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testGetUserReturnsUserIfUserExists() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();

        userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/Test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.email").value("Test@test.com")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.username").value("TestName")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.school").value("TestSchool")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.profilePictureUrl").value("testUrl")
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
        UserDto testUser = TestDataUtil.createValidTestUserDtoA();

        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/DoesNot@exist.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }

    @Test
    public void testFullUserUpdateHttpResponse400IfBlankEmailRequestBody() throws Exception {
        UserEntity testUser  = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        UserDto testUserDto = TestDataUtil.createTestUserDtoBlankEmail();
        String userJson = objectMapper.writeValueAsString(testUserDto);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/" + savedUser.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
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
                MockMvcRequestBuilders.put("/users/" + savedUser.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testFullUserUpdateSuccessfullyUpdatesUser() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        UserDto testUserDto = TestDataUtil.createValidTestUserDtoB();
        testUserDto.setEmail(savedUser.getEmail());
        String userJson = objectMapper.writeValueAsString(testUserDto);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/" + savedUser.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
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
    public void testPartialUpdateReturnHttp200IfUserExist() throws Exception {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        UserDto testUserDto = TestDataUtil.createValidTestUserDtoA();
        testUserDto.setUsername("UPDATED");
        String userJson = objectMapper.writeValueAsString(testUserDto);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/users/" + savedUser.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
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
                MockMvcRequestBuilders.patch("/users/" + savedUser.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
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
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/users/DoesNot@Exist.com")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testDeleteUserReturnsHttpStatus204ExistingUser() throws Exception{
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/users/" + savedUser.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

}
