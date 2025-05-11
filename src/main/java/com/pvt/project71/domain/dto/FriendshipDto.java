package com.pvt.project71.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendshipDto {

    private UserDto requester;

    private UserDto receiver;

    private Status status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate friendsSince;

}