package com.pvt.project71;

import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.services.ChallengeService;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private EventService eventService;

    @Test
    public void testSaveSavesValidUser(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        assertNotNull(savedUser);
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        assertEquals(testUser.getUsername(), savedUser.getUsername());
        assertEquals(testUser.getSchool(), savedUser.getSchool());

        Optional<UserEntity> retrievedUser = userService.findOne(savedUser.getEmail());
        assertTrue(retrievedUser.isPresent());
    }

    @Test
    public void testSaveCorrectlyAvoidsDuplicateEmails(){
        UserEntity testUserA = TestDataUtil.createValidTestUserEntity();
        userService.save(testUserA);

        UserEntity testUserB = TestDataUtil.createValidTestUserEntity();
        testUserB.setUsername("b");
        userService.save(testUserB);

        List<UserEntity> users = userService.findAll();

        assertEquals(1, users.size());
        assertEquals("Test@test.com", users.get(0).getEmail());
    }

    @Test
    public void testSaveThrowsExceptionBlankEmail(){
        UserEntity testUser = TestDataUtil.createInvalidTestUserEntity();
        assertThrows(IllegalArgumentException.class, () -> userService.save(testUser));
    }

    @Test
    public void testSaveThrowsExceptionNullEmail(){
        UserEntity testUser = TestDataUtil.createInvalidTestUserEntity();
        testUser.setEmail(null);
        assertThrows(IllegalArgumentException.class, () -> userService.save(testUser));
    }

    @Test
    public void testSaveThrowsExceptionNullUserEntity(){
        assertThrows(IllegalArgumentException.class, () -> userService.save(null));
    }

    @Test
    public void testDeleteDeletesUserWithNoChallengesOrEvents(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);
        userService.delete(testUser.getEmail());

        List<UserEntity> users = userService.findAll();
        assertEquals(0, users.size());
    }

    @Test
    public void testDeleteDeleteUsersWithChallenges(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();
        testChallenge.setCreator(testUser);
        challengeService.save(testChallenge);

        userService.delete(testUser.getEmail());

        List<UserEntity> users = userService.findAll();
        assertEquals(0, users.size());
    }

    @Test
    public void testDeleteDeleteUsersWithEventsAsAdmin(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        testEvent.setAdminUsers(List.of(testUser));
        eventService.save(testEvent);

        userService.delete(testUser.getEmail());

        List<UserEntity> users = userService.findAll();
        assertEquals(0, users.size());
    }

    @Test
    public void testDeleteThrowsExceptionNullEmail(){
        assertThrows(IllegalArgumentException.class, () -> userService.findOne(null));
    }

    @Test
    public void testDeleteThrowsExceptionEmptyEmail(){
        assertThrows(IllegalArgumentException.class, () -> userService.delete(""));
    }

    @Test
    public void testIsExistsCorrectlyReturnsTrue(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);
        assertTrue(userService.isExists(testUser.getEmail()));
    }

    @Test
    public void testIsExistsCorrectlyReturnsFalse(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        assertFalse(userService.isExists(testUser.getEmail()));
    }

    @Test
    public void testIsExistsThrowsExceptionNullEmail(){
        assertThrows(IllegalArgumentException.class, () -> userService.isExists(null));
    }

    @Test
    public void testIsExistsThrowsExceptionBlankEmail(){
        assertThrows(IllegalArgumentException.class, () -> userService.isExists(""));
    }

    @Test
    public void testFindAllReturnsUsers(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        UserEntity testUser2 = TestDataUtil.createValidTestUserEntity();
        testUser2.setEmail("TestUser2@test.com");
        userService.save(testUser2);

        List<UserEntity> users = userService.findAll();
        assertEquals(2, users.size());
    }

    @Test
    public void testFindAllReturnsNoUsers(){
        List<UserEntity> users = userService.findAll();
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testFindOneReturnsUser(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        UserEntity savedUser = userService.save(testUser);

        Optional<UserEntity> retrievedUser = userService.findOne(savedUser.getEmail());
        assertTrue(retrievedUser.isPresent());
    }

    @Test
    public void testFindOneThrowsExceptionNullEmail(){
        assertThrows(IllegalArgumentException.class, () -> userService.findOne(null));
    }

    @Test
    public void testFindOneThrowsExceptionBlankEmail(){
        assertThrows(IllegalArgumentException.class, () -> userService.findOne(""));
    }

    @Test
    public void testPartialUpdateOneAttribute(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        UserEntity newUser = TestDataUtil.createValidTestUserEntity();
        newUser.setUsername("Updated");
        UserEntity result = userService.partialUpdate(testUser.getEmail(), newUser);

        assertNotEquals("Updated", testUser.getUsername());
        assertEquals("Updated", result.getUsername());
    }

    @Test
    public void testPartialUpdateMultipleAttributes(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        UserEntity newUser = TestDataUtil.createValidTestUserEntity();
        newUser.setUsername("Updated");
        newUser.setSchool("Updated");
        UserEntity result = userService.partialUpdate(testUser.getEmail(), newUser);

        assertNotEquals("Updated", testUser.getUsername());
        assertNotEquals("Updated", testUser.getSchool());
        assertEquals("Updated", result.getUsername());
        assertEquals("Updated", result.getSchool());
    }

    @Test
    public void testPartialUpdateThrowsExceptionNullEmail(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        assertThrows(IllegalArgumentException.class, () -> userService.partialUpdate(null, testUser));
    }

    @Test
    public void testPartialUpdateThrowsExceptionNullUserEntity(){
        String testEmail = "testEmail";
        assertThrows(IllegalArgumentException.class, () -> userService.partialUpdate(testEmail, null));
    }

    @Test
    public void testPartialUpdateThrowsExceptionEmptyEmail(){
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        assertThrows(IllegalArgumentException.class, () -> userService.partialUpdate("", testUser));
    }

    @Test
    public void testPartialUpdateThrowsExceptionUserDoesntExist(){
        assertThrows(RuntimeException.class, () -> userService.partialUpdate("Doesnt@Exist.com", null));
    }


}
