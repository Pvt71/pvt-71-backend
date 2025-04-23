package com.pvt.project71;

import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.UserEntity;

public class UserControllerTestProperties {

    public static UserEntity createValidTestUserEntity(){
        return UserEntity.builder()
                .email("Test@test.com")
                .username("TestName")
                .school("TestSchool")
                .profilePictureUrl("testUrl")
                .build();
    }

    public static UserEntity createInvalidTestUserEntity(){
        return UserEntity.builder()
                .email("")
                .username("TestName")
                .school("TestSchool")
                .profilePictureUrl("testUrl")
                .build();
    }

    public static UserDto createValidTestUserDtoA(){
        return UserDto.builder()
                .email("Test@test.com")
                .username("TestName")
                .school("TestSchool")
                .profilePictureUrl("testUrl")
                .build();
    }

    public static UserDto createValidTestUserDtoB(){
        return UserDto.builder()
                .email("Test2@test.com")
                .username("TestName2")
                .school("TestSchool2")
                .profilePictureUrl("testUrl2")
                .build();
    }

    public static UserDto createTestUserDtoBlankEmail(){
        return UserDto.builder()
                .email("")
                .username("TestName2")
                .school("TestSchool2")
                .profilePictureUrl("testUrl2")
                .build();
    }

}
