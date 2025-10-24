package com.example.fas.repositories;

import com.example.fas.model.User;
import com.example.fas.enums.Social;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Check exists by username, identity card, phone number
    boolean existsByUsername(String username);

    boolean existsByIdentityCard(String identityCard);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByAvatarUrl(String avatarUrl);

    // Find by username, identity card, phone number
    User findByUsername(String username);

    User findByIdentityCard(String identityCard);

    User findByPhoneNumber(String phoneNumber);

    User findByEmail(String email);

    User findByAvatarUrl(String avatarUrl);

    User findByProviderId(String providerId);

    List<User> findByStatus(String status);

    List<User> findByRole(String role);

    // Match the entity field name 'provider' (enum Social)
    List<User> findByProvider(Social provider);

    // Query by username, identity card, phone number
    long count();
}
