package com.example.fas.repositories;

import com.example.fas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Check exists by username, identity card, phone number
    boolean existsByUsername(String username);

    boolean existsByIdentityCard(String identityCard);

    boolean existsByPhoneNumber(String phoneNumber);

    // Find by username, identity card, phone number
    User findByUsername(String username);

    User findByIdentityCard(String identityCard);

    User findByPhoneNumber(String phoneNumber);

    // Query by username, identity card, phone number
    long count();
}
