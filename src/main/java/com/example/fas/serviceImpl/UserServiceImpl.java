package com.example.fas.serviceImpl;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.dto.UserDto.UserUpdateRequest;
import com.example.fas.enums.Role;
import com.example.fas.enums.Status;
import com.example.fas.exceptions.user.error.HadUserActiveException;
import com.example.fas.exceptions.user.error.HadUserBannedException;
import com.example.fas.exceptions.user.error.HadUserDeteleException;
import com.example.fas.exceptions.user.exists.EmailExistsException;
import com.example.fas.exceptions.user.exists.IdentityCardExistsException;
import com.example.fas.exceptions.user.exists.PhoneNumberExistsException;
import com.example.fas.exceptions.user.exists.UsernameExistsException;
import com.example.fas.exceptions.user.invalid.*;
import com.example.fas.exceptions.user.notFound.AvatarUrlNotFoundException;
import com.example.fas.exceptions.user.notFound.EmailNotFoundException;
import com.example.fas.exceptions.user.notFound.ProviderIdNotFoundException;
import com.example.fas.exceptions.user.notFound.UserIDNotFoundException;
import com.example.fas.exceptions.user.notFound.UsernameNotFoundException;
import com.example.fas.mapper.UserMapper;
import com.example.fas.model.User;
import com.example.fas.repositories.UserRepository;
import com.example.fas.services.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
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

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(Status.ACTIVE);
        user.setRole(Role.USER);

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
        if (user == null) {
            throw new UserNotNullException("User cannot be null");
        }
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            throw new UsernameInvalidException("First name cannot be null or empty");
        }
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            throw new UsernameInvalidException("Last name cannot be null or empty");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().length() < 8) {
            throw new PasswordInvalidException("Password must be at least 8 characters");
        }
        if (!user.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$")) {
            throw new PasswordInvalidException("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            throw new PhoneNumberInvalidException("Phone number cannot be null or empty");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new EmailInvalidException("Email cannot be null or empty");
        }
//        if (user.getEmail().matches("\\b(?<num>[a-zA-Z0-9][\\w.-]{2,20}@[\\w-]{3,20}\\.[.\\w-]+)\\b")) {
//            throw new EmailInvalidException("Email is not valid");
//        }
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
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailExistsException("Email already exists");
        }
    }

    /*
     * This function validates the user ID during user operations.
     *
     * @param Long id - The user ID to validate.
     *
     * @return void - Throws exceptions if validation fails.
     */
    @Override
    public void validateUserId(Long id) {
        if (id == null || id <= 0) {
            throw new UserIDNotFoundException("User ID must be a positive number");
        }
        if (!userRepository.existsById(id)) {
            throw new UserIDNotFoundException("User with ID " + id + " does not exist");
        }
    }

    /**
     * This function validates the amount during balance operations.
     * *
     *
     * @param amount - The amount to validate.
     * @return void - Throws exceptions if validation fails.
     */
    @Override
    public void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AmountInvalidException("Amount must be a positive number");
        }
    }

    /*
     * This function updates the user details.
     *
     * @param Long id - The ID of the user to update.
     *
     * @param UserUpdateRequest - The new user details.
     *
     * @return User - The updated user details.
     *
     * Note: This method is currently unimplemented and throws an
     * UnsupportedOperationException.
     */
    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        validateUserId(id);
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    /*
     * This function restores a user by changing their status to ACTIVE.
     *
     * @param Long id - The ID of the user to restore.
     *
     * @return void - Throws exceptions if the user cannot be restored.
     */
    @Override
    @Transactional
    public void restoreUser(Long id) {
        User user = getUserEntityById(id);
        if (user.getStatus() == Status.BANNED) {
            throw new HadUserBannedException("User with ID " + id + " is not allowed to restore");
        } else if (user.getStatus() == Status.ACTIVE) {
            throw new HadUserActiveException("User with ID " + id + " is active");
        }
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
    }

    /**
     * This function use set role for user
     *
     * @param id - ID User
     * @return void - Nothing
     */
    @Override
    @Transactional
    public UserResponseDto isAdmin(Long id) {
        User user = getUserEntityById(id);
        if (!user.getRole().name().equals("ADMIN") && user.getStatus().name().equals("ACTIVE")) {
            user.setRole(Role.ADMIN);
        }
        return userMapper.toDto(userRepository.saveAndFlush(user));
    }

    /*
     * This function retrieves a user by their ID.
     *
     * @param Long id - The ID of the user to retrieve.
     *
     * @return UserResponseDto - The user details.
     */
    @Override
    public UserResponseDto getUserById(Long id) {
        return userMapper.toDto(getUserEntityById(id));
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
    public UserResponseDto getUserByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new PhoneNumberInvalidException("Phone number cannot be null or empty");
        }
        UserResponseDto userResponseDto = userMapper.toDto(userRepository.findByPhoneNumber(phoneNumber));
        if (userResponseDto == null) {
            throw new PhoneNumberInvalidException("User with phone number " + phoneNumber + " not found");
        }
        return userResponseDto;
    }

    @Override
    public UserResponseDto getUserByFullName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            throw new UsernameInvalidException("Full name cannot be null or empty");
        }
        if (!fullName.matches("^[a-zA-ZÀ-ỹ\\s]+$")) {
            throw new UsernameInvalidException("Full name contains invalid characters");
        }
        UserResponseDto userResponseDto = userMapper.toDto(userRepository.findByUsername(fullName));
        if (userResponseDto == null) {
            throw new UsernameNotFoundException("User with full name " + fullName + " not found");
        }
        return userResponseDto;
    }

    /*
     * This function deletes a user by their ID by setting their status to DELETED.
     *
     *
     * @param Long id - The ID of the user to delete.
     *
     * @return void - Throws exceptions if the user cannot be deleted.
     */
    @Override
    @Transactional
    public void deleteUserById(Long id) {
        User user = getUserEntityById(id);
        if (user.getStatus() == Status.DELETED) {
            throw new HadUserDeteleException("User with ID " + id + " is already deleted");
        } else if (user.getStatus() == Status.BANNED) {
            throw new HadUserDeteleException("User with ID " + id + " is banned, cannot delete");
        }
        user.setStatus(Status.DELETED);
        userRepository.save(user);
    }

    @Override
    @Transactional
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

    /*
     * This function deletes a user by their identity card number.
     *
     * @param String identityCard - The identity card number of the user to delete.
     *
     * @return void - Throws exceptions if the user cannot be deleted.
     */
    @Override
    @Transactional
    public void deleteUserByIdentityCard(String identityCard) {
        validateUserByIdentityCard(identityCard);
        UserResponseDto userResponseDto = getUserByIdentityCard(identityCard);
        User user = getUserEntityById(userResponseDto.getId());
        if (user.getStatus() == Status.DELETED) {
            throw new HadUserDeteleException("User with identity card " + identityCard + " is already deleted");
        } else if (user.getStatus() == Status.BANNED) {
            throw new HadUserDeteleException("User with identity card " + identityCard + " is banned, cannot delete");
        }
        user.setStatus(Status.DELETED);
        userRepository.save(user);
    }

    /**
     * This function retrieves the balance of a user by their ID.
     * *
     *
     * @param id - The ID of the user whose balance is to be retrieved.
     * @return BigDecimal - The balance of the user.
     * Throws exceptions if the user ID is invalid or the user is not found.
     */
    @Override
    public BigDecimal getBalanceById(Long id) {
        validateUserId(id);
        User user = getUserEntityById(id);
        return user.getBalance();
    }

    /**
     * This function updates the balance of a user by their ID.
     * *
     *
     * @param id         The ID of the user whose balance is to be updated.
     * @param newBalance - The new balance to set for the user.
     * @return void - Throws exceptions if the user ID is invalid, the user is not found,
     * or the new balance is invalid.
     */
    @Override
    @Transactional
    public void updateBalanceById(Long id, BigDecimal newBalance) {
        validateAmount(newBalance);
        User user = getUserEntityById(id);
        user.setBalance(newBalance);
    }

    /**
     * This function increases the balance of a user by a specified amount.
     * *
     *
     * @param id     - The ID of the user whose balance is to be increased.
     * @param amount - The amount to increase the user's balance by.
     * @return void - Throws exceptions if the user ID is invalid, the user is not found,
     * or the amount is invalid.
     */
    @Override
    @Transactional
    public void increaseBalance(Long id, BigDecimal amount) {
        User user = getUserEntityById(id);
        validateAmount(amount);
        user.setBalance(user.getBalance().add(amount));
    }


    /**
     * This function decreases the balance of a user by a specified amount.
     * *
     *
     * @param id     - The ID of the user whose balance is to be decreased.
     * @param amount - The amount to decrease the user's balance by.
     * @return void - Throws exceptions if the user ID is invalid, the user is not found,
     * or the amount is invalid.
     */
    @Override
    @Transactional
    public void decreaseBalance(Long id, BigDecimal amount) {
        validateAmount(amount);
        User user = getUserEntityById(id);
        user.setBalance(user.getBalance().subtract(amount));
    }

    /**
     * This function validates the user details during an update operation.
     *
     * @param user user - The user details to validate.
     */
    @Override
    @Transactional
    public void validateUpdateUser(UserUpdateRequest user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateUpdateUser'");
    }

    /**
     * This function retrieves a user entity by their ID.
     * *
     *
     * @param id - The ID of the user to retrieve.
     * @return User - The user entity.
     * Throws exceptions if the user ID is invalid or the user is not found.
     */
    @Override
    public User getUserEntityById(Long id) {
        validateUserId(id);
        return userRepository.findById(id).orElseThrow(() -> new UserIDNotFoundException("User with ID " + id + " not found"));
    }

    /*
     * This function retrieves all users in the system.
     *
     * @return List<UserResponseDto> - A list of all users' details.
     */
    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toDtoList(users);
    }

    /*
     * This function validates a user by their identity card number.
     *
     * @param String identityCard - The identity card number to validate.
     *
     * @return void - Throws exceptions if validation fails.
     */
    @Override
    public void validateUserByIdentityCard(String identityCard) {
        if (identityCard == null || identityCard.isEmpty()) {
            throw new IdentityCardInvalidException("Identity card cannot be null or empty");
        }
        if (identityCard.length() != 12) {
            throw new IdentityCardInvalidException("Identity card must be exactly 12 digits long");
        }
        if (!identityCard.matches("^\\d{12}$")) {
            throw new IdentityCardInvalidException("Identity card must contain only digits");
        }
        if (!userRepository.existsByIdentityCard(identityCard)) {
            throw new IdentityCardInvalidException("User with identity card " + identityCard + " does not exist");
        }
    }

    /*
     * This function retrieves a user by their email address.
     * *
     *
     * @param String email - The email address of the user to retrieve.
     *
     * @return UserResponseDto - The user details.
     * Throws exceptions if the email is invalid or the user is not found.
     */
    @Override
    public UserResponseDto getUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new EmailInvalidException("Email cannot be null or empty");
        }
        if (email.matches("\\b(?<num>[a-zA-Z0-9][\\w.-]{2,20}@[\\w-]{3,20}\\.[.\\w-]+)\\b")) {
            throw new EmailInvalidException("Email is not valid");
        }
        UserResponseDto userResponseDto = userMapper.toDto(userRepository.findByEmail(email));
        if (userResponseDto == null) {
            throw new EmailNotFoundException("User with email " + email + " not found");
        }
        return userResponseDto;
    }

    /*
     * This function retrieves a user by their avatar URL.
     * *
     *
     * @param String avatarUrl - The avatar URL of the user to retrieve.
     *
     * @return UserResponseDto - The user details.
     * Throws exceptions if the avatar URL is invalid or the user is not found.
     */
    @Override
    public UserResponseDto getUserByAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            throw new AvatarUrlInvalidException("Avatar URL cannot be null or empty");
        }
        UserResponseDto userResponseDto = userMapper.toDto(userRepository.findByAvatarUrl(avatarUrl));
        if (userResponseDto == null) {
            throw new AvatarUrlNotFoundException("User with avatar URL " + avatarUrl + " not found");
        }
        return userResponseDto;
    }

    @Override
    public UserResponseDto getUserByProviderId(String providerId) {
        if (providerId == null || providerId.isEmpty()) {
            throw new ProviderIdInvalidException("Provider ID cannot be null or empty");
        }
        UserResponseDto userResponseDto = userMapper.toDto(userRepository.findByProviderId(providerId));
        if (userResponseDto == null) {
            throw new ProviderIdNotFoundException("User with provider ID " + providerId + " not found");
        }
        return userResponseDto;
    }

    /*
     * */
    @Override
    public Set<UserResponseDto> getUsersByStatus(String status) {
        return userMapper.toDtoSet(userRepository.findByStatus(status));
    }

    @Override
    public Set<UserResponseDto> getUsersByRole(String role) {
        return userMapper.toDtoSet(userRepository.findByRole(role));
    }

    @Override
    public Set<UserResponseDto> getUsersBySocialProvider(String provider) {
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("Provider cannot be null or empty");
        }
        try {
            com.example.fas.enums.Social social = com.example.fas.enums.Social.valueOf(provider.toUpperCase());
            return userMapper.toDtoSet(userRepository.findByProvider(social));
        } catch (IllegalArgumentException ex) {
            throw new com.example.fas.exceptions.user.invalid.UsernameInvalidException("Invalid social provider: " + provider);
        }
    }
}
