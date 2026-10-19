package com.irctc.userservice.DTO;

import com.irctc.userservice.Entity.User;

import java.util.UUID;

public record LoggedInUserDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Boolean emailVerified
) {
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
