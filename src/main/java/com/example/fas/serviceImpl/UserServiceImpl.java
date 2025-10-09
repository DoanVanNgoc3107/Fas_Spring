package com.example.fas.serviceImpl;

import com.example.fas.exceptions.user.invalid.UsernameInvalidException;
import com.example.fas.model.User;
import com.example.fas.repositories.UserRepository;
import com.example.fas.services.UserService;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

	@Override
	public void validateUser(User user) {
		if (user.getFullName() == null)
		throw new UsernameInvalidException("Unimplemented method 'validateUser'");
	}
}
