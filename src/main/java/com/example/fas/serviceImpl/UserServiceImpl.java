package com.example.fas.serviceImpl;

import com.example.fas.repositories.UserRepository;
import com.example.fas.services.UserService;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
