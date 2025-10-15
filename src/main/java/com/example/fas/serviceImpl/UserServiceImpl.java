package com.example.fas.serviceImpl;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.dto.UserDto.UserUpdateRequest;
import com.example.fas.enums.Role;
import com.example.fas.enums.Status;
import com.example.fas.exceptions.user.error.HadUserActiveException;
import com.example.fas.exceptions.user.error.HadUserDeteleException;
import com.example.fas.exceptions.user.exists.CitizenIdExistsException;
import com.example.fas.exceptions.user.exists.IdentityCardExistsException;
import com.example.fas.exceptions.user.exists.PhoneNumberExistsException;
import com.example.fas.exceptions.user.exists.UsernameExistsException;
import com.example.fas.exceptions.user.invalid.CitizenIdInvalidException;
import com.example.fas.exceptions.user.invalid.PasswordInvalidException;
import com.example.fas.exceptions.user.invalid.PhoneNumberInvalidException;
import com.example.fas.exceptions.user.invalid.UsernameInvalidException;

import com.example.fas.exceptions.user.notFound.UserIDNotFoundException;
import com.example.fas.mapper.UserMapper;
import com.example.fas.model.User;
import com.example.fas.repositories.UserRepository;
import com.example.fas.services.UserService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private final UserMapper userMapper;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
            BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * This function creates a new user in the system.
     * 
     * @param UserRequestDto dto - The user details for the new user.
     * 
     * @return UserResponseDto - The created user's details.
     */
    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto dto) {
        validateUser(dto);
        String citizenIdString = User.generateRandomAlphanumericIdentityCard();
        validateCitizenId(citizenIdString);
        User user = User.builder()
                .fullName(dto.getFirstName() + " " + dto.getLastName())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phoneNumber(dto.getPhoneNumber())
                .identityCard(dto.getIdentityCard())
                .status(Status.ACTIVE)
                .role(Role.RESIDENT)
                .citizenId(citizenIdString)
                .build();

        return userMapper.toDto(userRepository.saveAndFlush(user));
    }

    /*
     * This function validates the user details during creation.
     * 
     * @param UserRequestDto user - The user details to validate.
     * 
     * @return void - Throws exceptions if validation fails.
     */
    @Override
    public void validateUser(UserRequestDto user) {
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            throw new UsernameInvalidException("First name cannot be null or empty");
        }
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            throw new UsernameInvalidException("Last name cannot be null or empty");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty() ||
                user.getPassword().length() < 8) {
            throw new PasswordInvalidException("Password must be at least 8 characters");
        }
        if (!user.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\\\d)[A-Za-z\\\\d]{8,}$")) {
            throw new PasswordInvalidException(
                    "Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        }
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            throw new PhoneNumberInvalidException("Phone number cannot be null or empty");
        }
        if (user.getPhoneNumber().length() != 10) {
            throw new PhoneNumberInvalidException("Phone number must be exactly 10 digits long");
        }
        if (!user.getPhoneNumber().matches("^(\\+84|0)(3[2-9]|5[689]|7[0-9]|8[1-5]|9[0-46-9])[0-9]{7}$")) {
            throw new PhoneNumberInvalidException("Phone number is not valid in Vietnam");
        }
        if (user.getIdentityCard() == null || user.getIdentityCard().isEmpty()) {
            throw new PhoneNumberInvalidException("Identity card cannot be null or empty");
        }
        if (user.getIdentityCard().length() != 12) {
            throw new PhoneNumberInvalidException("Identity card must be exactly 12 digits long");
        }
        if (!user.getIdentityCard().matches("^\\d{12}$")) {
            throw new PhoneNumberInvalidException("Identity card must contain only digits");
        }

        // Validate of repository
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameExistsException("Username already exists");
        }
        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new PhoneNumberExistsException("Phone number already exists");
        }
        if (userRepository.existsByIdentityCard(user.getIdentityCard())) {
            throw new IdentityCardExistsException("Identity card already exists");
        }
    }

    /*
     * This function validates the citizen ID during user creation.
     * 
     * @param String citizenId - The citizen ID to validate.
     * 
     * @return void - Throws exceptions if validation fails.
     */
    @Override
    public void validateCitizenId(String citizenId) {
        if (citizenId == null || citizenId.isEmpty()) {
            throw new CitizenIdInvalidException("Citizen ID cannot be null or empty");
        }
        if (citizenId.length() != 10) {
            throw new CitizenIdInvalidException("Citizen ID must be exactly 10 digits long");
        }
        if (!citizenId.matches("^[0-9]{10}$")) {
            throw new CitizenIdInvalidException("Citizen ID must contain only digits");
        }
        if (userRepository.existsByCitizenId(citizenId)) {
            throw new CitizenIdExistsException("Citizen ID already exists");
        }
    }

    /*
    * This function validates the user ID during user operations.
    *
    * @param Long id - The user ID to validate.
    * @return void - Throws exceptions if validation fails.
    * */
    @Override
    public void validateUserId(Long id) {
        if (id == null || id <= 0) {
            throw new UserIDNotFoundException("User ID must be a positive number");
        }
        if (!userRepository.existsById(id)) {
            throw new UserIDNotFoundException("User with ID " + id + " does not exist");
        }
    }

    @Override
    public User updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        validateUserId(id);
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    /*
    * */
    @Override
    public void restoreUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserIDNotFoundException("User with ID " + id + " not found"));
        if (user.getStatus() == Status.BANNED) {
            throw new IllegalStateException("User with ID " + id + " is not allowed to restore");
        } else if (user.getStatus() == Status.ACTIVE) {
            throw new HadUserActiveException("User with ID " + id + " is active");
        }
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new UserIDNotFoundException("User ID must be a positive number");
        }
        return userMapper.toDto(
                userRepository.findById(id).orElseThrow(
                        () -> new UserIDNotFoundException("User with ID " + id + " not found")));
    }

    @Override
    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameInvalidException("User with username " + username + " not found");
        }
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getUserByIdentityCard(String identityCard) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserByIdentityCard'");
    }

    @Override
    public UserResponseDto getUserByCitizenId(String citizenId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserByCitizenId'");
    }

    @Override
    public UserResponseDto getUserByPhoneNumber(String phoneNumber) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserByPhoneNumber'");
    }

    @Override
    public UserResponseDto getUserByFullName(String fullName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserByFullName'");
    }

    @Override
    public void deleteUserById(Long id) {
        validateUserId(id);
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserIDNotFoundException("User with ID " + id + " not found"));
        if (user.getStatus() == Status.DELETED) {
            throw new HadUserDeteleException("User with ID " + id + " is already deleted");
        } else if (user.getStatus() == Status.BANNED) {
            throw new HadUserDeteleException("User with ID " + id + " is banned, cannot delete");
        }
        user.setStatus(Status.DELETED);
        userRepository.save(user);
    }

    @Override
    public void deleteUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameInvalidException("User with username " + username + " not found");
        }
        if (user.getStatus() == Status.DELETED) {
            throw new HadUserDeteleException("User with username " + username + " is already deleted");
        } else if (user.getStatus() == Status.BANNED) {
            throw new HadUserDeteleException("User with username " + username + " is banned, cannot delete");
        }
        user.setStatus(Status.DELETED);
        userRepository.save(user);
    }

    @Override
    public void deleteUserByIdentityCard(String identityCard) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUserByIdentityCard'");
    }

    @Override
    public void deleteUserByCitizenId(String citizenId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUserByCitizenId'");
    }

    @Override
    public void validateUpdateUser(UserUpdateRequest user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateUpdateUser'");
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toDtoList(users);
    }
}
