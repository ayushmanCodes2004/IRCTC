package com.irctc.userservice.dto;

import com.irctc.userservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponse {

    private Boolean success;

    private String message;

    private User data;

}
