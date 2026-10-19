package com.irctc.userservice.Service;

import com.irctc.userservice.Entity.User;
import com.irctc.userservice.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService {

    private final UserRepository userRepository;

    public Optional<User> findByProviderAndSubject(String provider, String subject) {
        return userRepository.findByProviderAndProviderSubject(provider, subject);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User createOrUpdateOAuthUser(String provider, String subject, String email, String firstName, String lastName) {
        Optional<User> existingUser = userRepository.findByProviderAndProviderSubject(provider, subject);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmailVerified(true);
            return userRepository.save(user);
        } else {
            User newUser = new User();
            newUser.setProvider(provider);
            newUser.setProviderSubject(subject);
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmailVerified(true);
            newUser.setPassword(null);
            return userRepository.save(newUser);
        }
    }
}
