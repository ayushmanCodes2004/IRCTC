package com.irctc.userservice.DTO;

import com.irctc.userservice.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponseDTO {

    private Boolean success;

    private String message;

    private User data;

}
