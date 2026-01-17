package com.example.fas.repositories;

import com.example.fas.model.User;
import com.example.fas.model.enums.oauth2.AuthProvider;
import com.example.fas.model.enums.user.UserStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Check exists by username, identity card, phone number
    boolean existsByUsername(String username);

//    boolean existsByIdentityCard(String identityCard);

//    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByAvatarUrl(String avatarUrl);

    boolean existsByFullName(String fullName);

    // Find by username, identity card, phone number
    @EntityGraph(attributePaths = {"role"})
    User findByUsername(String username);

    @Query("select u from User u join fetch u.role where u.username = :username")
    User findByUsernameWithRole(@Param("username") String username);

//    User findByIdentityCard(String identityCard);
//
//    User findByPhoneNumber(String phoneNumber);

    User findByEmail(String email);

    User findByAvatarUrl(String avatarUrl);

    User findByFullName(String fullName);

    User findByProviderId(String providerId);

    List<User> findByUserStatus(UserStatus userStatus);

    // Match the entity field name 'provider' (enum AuthProvider)
    List<User> findByProvider(AuthProvider provider);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerSpecificId);

    // Query by username, identity card, phone number
    long count();
}
