package com.example.fas.repositories;

import com.example.fas.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);
}
