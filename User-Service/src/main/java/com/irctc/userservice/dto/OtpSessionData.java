package com.irctc.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtpSessionData {

    private String hashedOtp;
    private String firstName;
    private String lastName;
    private String email;
    private String hashedPassword;

}
