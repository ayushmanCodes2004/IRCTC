package com.irctc.userservice.Repository;

import com.irctc.userservice.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByProviderAndProviderSubject(String provider, String providerSubject);
    
    boolean existsByProviderAndProviderSubject(String provider, String providerSubject);
}
