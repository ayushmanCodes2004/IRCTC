package com.irctc.userservice.service;

import com.irctc.userservice.dto.OtpResult;
import com.irctc.userservice.dto.OtpSessionData;
import com.irctc.userservice.dto.RegisterRequest;
import com.irctc.userservice.entity.User;
import com.irctc.userservice.error.ConflictError;
import com.irctc.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final OtpService otpService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public OtpResult sendOtp(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictError("User already exists with email: " + request.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        return otpService.generateAndStoreOtp(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            hashedPassword
        );

    }

    public User verifyOtp(String otpSessionId, String otp) {

        OtpSessionData otpSessionData = otpService.verifyOtp(otpSessionId, otp);

        if(otpSessionData == null) {
            throw new ConflictError("Invalid OTP or OTP has expired.");
        }
        User user = new User();
        user.setFirstName(otpSessionData.getFirstName());
        user.setLastName(otpSessionData.getLastName());
        user.setEmail(otpSessionData.getEmail());
        user.setPassword(otpSessionData.getHashedPassword());
        user.setEmailVerified(true);

        userRepository.save(user);

        return user;





    }

    

}
