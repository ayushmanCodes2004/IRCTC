package com.irctc.userservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtpSessionDataDTO {

    private String hashedOtp;
    private String firstName;
    private String lastName;
    private String email;
    private String hashedPassword;

}
