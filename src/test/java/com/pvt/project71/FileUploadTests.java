package com.pvt.project71;

import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FileUploadTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;
    @Autowired
    private EventService eventService;

    private Jwt getUserToken() {
        return jwtService.mockOauth2(TestDataUtil.createValidTestUserEntity(),1, ChronoUnit.MINUTES);
    }

    private Jwt getInvalidUserToken() {
        return jwtService.mockOauth2(TestDataUtil.createInvalidTestUserEntity(),1, ChronoUnit.MINUTES);
    }


    @Test
    public void testUploadEventBannerWithValidJwtShouldSucceed() throws Exception {
        // Arrange
        byte[] imageBytes = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "banner.jpg", "image/jpeg", imageBytes);

        Jwt jwt = getUserToken();

        // Save test user and event first (this assumes user has admin rights on event with ID 1)
        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);

        EventEntity event = TestDataUtil.createTestEventEntityA();
        event.setAdminUsers(List.of(user));
        eventService.save(event, user);

        // Act
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/events/" + event.getId() + "/banner")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(status().isOk());
    }

    @Test
    public void testUploadEventBannerWithInvalidJwtIsUnauthorized() throws Exception {
        byte[] imageBytes = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "banner.jpg", "image/jpeg", imageBytes);

        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);

        EventEntity event = TestDataUtil.createTestEventEntityA();
        event.setAdminUsers(List.of(user));
        eventService.save(event, user);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/events/" + event.getId() + "/banner")
                        .file(file)
                        .header("Authorization", "Bearer bad.token.value")) // NotarealJWT
                .andExpect(status().isUnauthorized());
    }
    @Test
    public void testUploadWithoutTokenReturnsUnauthorized() throws Exception {
        byte[] imageBytes = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "banner.jpg", "image/jpeg", imageBytes);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/events/"+eventService.getDefaultEvent(TestDataUtil.SCHOOL_NAME).getId()+"/banner")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUploadEventBannerWithInvalidFileTypeShouldFail() throws Exception {
        byte[] invalidFileBytes = "NotValid".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "invalid.txt", "text/plain", invalidFileBytes);

        Jwt jwt = getUserToken();

        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);

        EventEntity event = TestDataUtil.createTestEventEntityA();
        event.setAdminUsers(List.of(user));
        eventService.save(event, user);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/events/" + event.getId() + "/banner")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUploadEventBannerTooLargeFileReturnsBadRequest() throws Exception {
        byte[] largeImage = new byte[11 * 1024 * 1024]; // 11 MB
        MockMultipartFile file = new MockMultipartFile("file", "large.jpg", "image/jpeg", largeImage);

        Jwt jwt = getUserToken();
        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);
        EventEntity event = TestDataUtil.createTestEventEntityA();
        event.setAdminUsers(List.of(user));
        eventService.save(event, user);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/events/" + event.getId() + "/banner")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetEventBannerShouldReturnImage() throws Exception {
        byte[] imageBytes = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "banner.jpg", "image/jpeg", imageBytes);

        Jwt jwt = getUserToken();

        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);

        EventEntity event = TestDataUtil.createTestEventEntityA();
        event.setAdminUsers(List.of(user));
        eventService.save(event, user);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/events/" + event.getId() + "/banner")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/uploads/events/" + event.getId() + "/banner"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }
    @Test
    public void testGetEventBannerWithInvalidIdShouldReturnNotFound() throws Exception {
        // Act
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/uploads/events/999/banner"))
                .andExpect(status().isNotFound());
    }
    @Test
    public void testDeleteEventBannerShouldSucceed() throws Exception {
        byte[] imageBytes = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "banner.jpg", "image/jpeg", imageBytes);

        Jwt jwt = getUserToken();

        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);

        EventEntity event = TestDataUtil.createTestEventEntityA();
        event.setAdminUsers(List.of(user));
        eventService.save(event, user);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/events/" + event.getId() + "/banner")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/uploads/events/" + event.getId() + "/banner"))
                .andExpect(status().isNoContent());
    }
    @Test
    public void testDeleteEventBannerWithInvalidIdShouldReturnNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/uploads/events/999/banner"))
                .andExpect(status().isNotFound());
    }


    @Test
    public void testUploadProfilePictureWithValidJwtShouldSucceed() throws Exception {
        Jwt jwt = getUserToken();
        byte[] imageBytes = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", imageBytes);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/users/Test@test.com/profilePicture")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(status().isOk());
    }

    @Test
    public void testUploadProfilePictureWithInvalidJwtShouldBeUnauthorized() throws Exception {
        byte[] imageBytes = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", imageBytes);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/users/Test@test.com/profilePicture")
                        .file(file)
                        .header("Authorization", "Bearer bad.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUploadProfilePictureWithInvalidFileTypeShouldReturnBadRequest() throws Exception {
        Jwt jwt = getUserToken();
        MockMultipartFile file = new MockMultipartFile("file", "invalid.txt", "text/plain", "fake text".getBytes());

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/users/Test@test.com/profilePicture")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetProfilePictureShouldReturnImage() throws Exception {
        Jwt jwt = getUserToken();
        byte[] imageBytes = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", imageBytes);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/users/Test@test.com/profilePicture")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/uploads/users/Test@test.com/profilePicture"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }

    @Test
    public void testDeleteProfilePictureShouldSucceed() throws Exception {
        Jwt jwt = getUserToken();
        byte[] imageBytes = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", imageBytes);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/users/Test@test.com/profilePicture")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/uploads/users/Test@test.com/profilePicture"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUploadAndRetrieveEventBannerBLOB() throws Exception {
        byte[] imageBytes = new byte[]{10, 20, 30, 40, 50};
        MockMultipartFile file = new MockMultipartFile("file", "banner.jpg", "image/jpeg", imageBytes);

        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);

        EventEntity event = TestDataUtil.createTestEventEntityA();
        event.setAdminUsers(List.of(user));
        eventService.save(event, user);

        Jwt jwt = jwtService.mockOauth2(user, 5, ChronoUnit.MINUTES);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/events/" + event.getId() + "/banner")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(status().isOk());

        byte[] retrievedImageBytes = mockMvc.perform(MockMvcRequestBuilders
                        .get("/uploads/events/" + event.getId() + "/banner"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertArrayEquals(imageBytes, retrievedImageBytes, "Retrieved image should match uploaded image");
    }

    @Test
    public void testUploadAndRetrieveProfilePictureBLOB() throws Exception {
        byte[] imageBytes = new byte[]{10, 20, 30, 40, 50};
        MockMultipartFile file = new MockMultipartFile("file", "pf.jpg", "image/jpeg", imageBytes);

        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);


        Jwt jwt = jwtService.mockOauth2(user, 5, ChronoUnit.MINUTES);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/users/" + user.getEmail() + "/profilePicture")
                        .file(file)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()))
                .andExpect(status().isOk());

        byte[] retrievedImageBytes = mockMvc.perform(MockMvcRequestBuilders
                        .get("/uploads/users/" + user.getEmail() + "/profilePicture"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertArrayEquals(imageBytes, retrievedImageBytes, "Retrieved image should match uploaded image");
    }


}