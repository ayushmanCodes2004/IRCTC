package com.irctc.userservice.DTO;

import com.irctc.userservice.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoggedInUserDTO {
    
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean emailVerified;

    public static LoggedInUserDTO from(User user) {
        return new LoggedInUserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getEmailVerified()
        );
    }
}
