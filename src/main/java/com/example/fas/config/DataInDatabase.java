package com.example.fas.config;

import com.example.fas.mapper.dto.RoleDto.RoleRequestDto;
import com.example.fas.model.User;
import com.example.fas.model.enums.user.UserStatus;
import com.example.fas.repositories.RoleRepository;
import com.example.fas.repositories.UserRepository;
import com.example.fas.repositories.services.serviceImpl.RoleServiceImp;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInDatabase {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Initialize roles and users in the database
    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, RoleRepository roleRepository, RoleServiceImp roleServiceImp) {
        return args -> {
            if (roleRepository.findByRoleName("ADMIN") == null) {
                roleServiceImp.createRole(new RoleRequestDto("ADMIN", "Administrator with full access"));
            }
            if (roleRepository.findByRoleName("USER") == null) {
                roleServiceImp.createRole(new RoleRequestDto("USER", "Standard user with limited access"));
            }

            // User initialization can be added here if needed
            if (userRepository.count() == 0) {
                // ADMIN
                userRepository.save(User.builder()
                        .fullName("DOAN VAN NGOC")
                        .username("admin")
                        .password(passwordEncoder.encode("31072005An.!"))
//                        .phoneNumber("0345515987")
                        .email("admin@gmail.com")
//                        .identityCard("012345678912")
//                        .isPremium(true)
//                        .balance(BigDecimal.valueOf(10000000))
//                        .coins(99999)
                        .userStatus(UserStatus.ACTIVE)
                        .role(roleRepository.findByRoleName("ADMIN"))
                        .build()
                );

                // USER
                userRepository.save(User.builder()
                        .fullName("DOAN BINH AN")
                        .username("user")
                        .password(passwordEncoder.encode("31072005An.!"))
//                        .phoneNumber("0987654321")
                        .email("user@gmail.com")
//                        .isPremium(false)
//                        .identityCard("987654321012")
                        .role(roleRepository.findByRoleName("USER"))
                        .userStatus(UserStatus.ACTIVE)
//                        .phoneNumber("0345515986")
                        .build());
            }
        };
    }
}
