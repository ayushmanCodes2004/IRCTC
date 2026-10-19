package com.irctc.userservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtpResponseDTO {

    private boolean success;
    private String message;
    private OtpResultDTO otpResultDTO;
}