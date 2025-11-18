package com.example.fas.repositories;

import com.example.fas.model.User;
import com.example.fas.enums.oauth2.AuthProvider;
import com.example.fas.enums.user.UserStatus;

import java.util.List;
import java.util.Optional;

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

    List<User> findByUserStatus(UserStatus userStatus);

    List<User> findByRole(String role);

    // Match the entity field name 'provider' (enum AuthProvider)
    List<User> findByProvider(AuthProvider provider);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerSpecificId);

    // Query by username, identity card, phone number
    long count();
}
