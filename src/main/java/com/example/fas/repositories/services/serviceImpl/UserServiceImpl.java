package com.example.fas.repositories.services.serviceImpl;

import com.example.fas.mapper.dto.UserDto.UserRequestDto;
import com.example.fas.mapper.dto.UserDto.UserResponseDto;
import com.example.fas.mapper.dto.UserDto.UserUpdateRequest;
import com.example.fas.model.enums.oauth2.AuthProvider;
import com.example.fas.model.enums.user.UserStatus;
import com.example.fas.repositories.services.serviceImpl.exceptions.auth.AccessTokenInvalidException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.error.HadUserActiveException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.error.HadUserBannedException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.error.HadUserDeteleException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.error.HadUserRoleAdminException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.exists.EmailExistsException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.exists.IdentityCardExistsException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.exists.PhoneNumberExistsException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.exists.UsernameExistsException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.invalid.*;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.notFound.AvatarUrlNotFoundException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.notFound.EmailNotFoundException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.notFound.ProviderIdNotFoundException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.notFound.UserIDNotFoundException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.notFound.UsernameNotFoundException;
import com.example.fas.mapper.UserMapper;
import com.example.fas.model.User;
import com.example.fas.repositories.RoleRepository;
import com.example.fas.repositories.UserRepository;
import com.example.fas.repositories.services.UserService;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

@Service
public class UserServiceImpl implements UserService {

    String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           BCryptPasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto dto) {
        validateUser(dto);

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setUserStatus(UserStatus.ACTIVE);
        user.setPremium(false);
        user.setRole(roleRepository.findByRoleName("USER"));

        return userMapper.toDto(userRepository.saveAndFlush(user));
    }

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
        // Validate password contains at least one uppercase letter, one lowercase letter, one digit, and one special character
        if (!user.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$")) {
            throw new PasswordInvalidException(
                    "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }
        // Thoát dấu chấm bằng \\. để đảm bảo an toàn trong Java String
        if (!user.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&\\.])[A-Za-z\\d@$!%*?&\\.]{8,}$")) {
            throw new PasswordInvalidException(
                    "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
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
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new EmailInvalidException("Email cannot be null or empty");
        }
        if (!user.getEmail().matches(emailRegex)) {
            throw new EmailInvalidException("Email is not valid");
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
        if (userRepository.existsByEmail(user.getEmail().trim().toLowerCase())) {
            throw new EmailExistsException("Email already exists");
        }
    }

    /**
     * @param id
     *
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
     * This function validates a monetary amount.
     *
     * @param amount - The monetary amount to validate. (VND)
     *               Chuẩn hóa số tiền: Loại VND không có chữ số thập phân, ví dụ 10000.50 là không hợp lệ
     *               Số tiền phải là số dương lớn hơn 0
     */
    @Override
    public void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.scale() == 0) {
            throw new AmountInvalidException("Amount must be a positive number and cannot have decimal places (VND)");
        }
    }

    /**
     * This function updates a user's information.
     *
     * @param userUpdateRequest - The user update request containing the new information.
     * @return UserResponseDto - The updated user details.
     */
    @Override
    @Transactional
    public UserResponseDto updateUser(UserUpdateRequest userUpdateRequest) {

        User user = getUserEntityById(userUpdateRequest.getId());

        if (!userUpdateRequest.getFirstName().isEmpty() || !userUpdateRequest.getLastName().isEmpty()) {
            user.setFullName(userUpdateRequest.getLastName() + " " + userUpdateRequest.getFirstName());
        }

        if (!userUpdateRequest.getPassword().isEmpty() && userUpdateRequest.getPassword().length() >= 8
                && userUpdateRequest.getPassword()
                .matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$")) {
            user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        }

        if (!userUpdateRequest.getPhoneNumber().isEmpty() && userUpdateRequest.getPhoneNumber()
                .matches("^(\\+84|0)(3[2-9]|5[689]|7[0-9]|8[1-5]|9[0-46-9])[0-9]{7}$")) {
            user.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        }

        if (!userUpdateRequest.getEmail().isEmpty()) {
            String normalized = userUpdateRequest.getEmail().trim().toLowerCase();
            if (!normalized.matches(emailRegex)) {
                throw new EmailInvalidException("Email is not valid");
            }
            if (userRepository.existsByEmail(normalized) && !normalized.equals(user.getEmail())) {
                throw new EmailExistsException("Email already exists");
            }
            user.setEmail(normalized);
        }

        if (!userUpdateRequest.getIdentityCard().isEmpty()
                && userUpdateRequest.getIdentityCard().matches("^\\d{12}$")) {
            user.setIdentityCard(userUpdateRequest.getIdentityCard());
        }

        if (!userUpdateRequest.getAvatarUrl().isEmpty()) {
            user.setAvatarUrl(userUpdateRequest.getAvatarUrl());
        }

        return userMapper.toDto(userRepository.saveAndFlush(user));
    }

    /**
     * This function restores a user having been soft-deleted
     *
     * @param id id - The ID of the user to be restored.
     */
    @Override
    @Transactional
    public void restoreUser(Long id) {
        User user = getUserEntityById(id);
        if (user.getUserStatus() == UserStatus.BANNED_PERMANENT) {
            throw new HadUserBannedException("User with ID " + id + " is not allowed to restore");
        } else if (user.getUserStatus() == UserStatus.ACTIVE) {
            throw new HadUserActiveException("User with ID " + id + " is active");
        }
        user.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    /**
     * This function set role ADMIN for user
     *
     * @param id id - The ID of the user to be set as admin.
     * @return UserResponseDto
     */
    @Override
    @Transactional
    public UserResponseDto isAdmin(Long id) {
        User user = getUserEntityById(id);
        if (!user.getRole().getRoleName().equals("ADMIN") && user.getUserStatus().name().equals("ACTIVE")) {
            user.setRole(roleRepository.findByRoleName("ADMIN"));
        } else {
            throw new HadUserRoleAdminException("User with ID " + id + " is not allowed to be admin");
        }
        return userMapper.toDto(userRepository.saveAndFlush(user));
    }

    /**
     * This function set role USER for user
     *
     * @param id The ID of the user to be set as user.
     * @return UserResponseDto
     */
    @Override
    @Transactional
    public UserResponseDto isUser(Long id) {
        User user = getUserEntityById(id);
        if (!user.getRole().getRoleName().equals("USER") && user.getUserStatus().name().equals("ACTIVE")) {
            user.setRole(roleRepository.findByRoleName("USER"));
        }
        return userMapper.toDto(userRepository.saveAndFlush(user));
    }



    @Override
    @Transactional
    public void bannedUser(Long id, int bannedDays) {
        validateUserId(id);

        User user = getUserEntityById(id);

        if (bannedDays <= 0 && bannedDays != -1) {
            throw new DayInvalidException("Days must be a positive number");
        }
        switch (bannedDays) {
            case 1: // Banned 1 day
                user.setUserStatus(UserStatus.BANNED_1_DAY);
                userRepository.save(user);
                break;
            case 7: // Banned 7 days
                user.setUserStatus(UserStatus.BANNED_7_DAYS);
                userRepository.save(user);
                break;
            case 30: // Banned 30 days
                user.setUserStatus(UserStatus.BANNED_30_DAYS);
                userRepository.save(user);
                break;
            case -1: // Banned permanent
                user.setUserStatus(UserStatus.BANNED_PERMANENT);
                userRepository.save(user);
                break;
            default:
                throw new DayInvalidException("Only support 1, 7, 30 days");
        }
    }

    /**
     * This function unbans a user by their ID.
     *
     * @param id The ID of the user to be unbanned.
     */
    @Override
    public void unbannedUser(Long id) {
        validateUserId(id);
        User user = getUserEntityById(id);
        if (user.getUserStatus() == UserStatus.BANNED_1_DAY || user.getUserStatus() == UserStatus.BANNED_7_DAYS ||
                user.getUserStatus() == UserStatus.BANNED_30_DAYS ||
                user.getUserStatus() == UserStatus.BANNED_PERMANENT) {
            user.setUserStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        } else if (user.getUserStatus() == UserStatus.ACTIVE || user.getUserStatus() == UserStatus.DELETED) {
            throw new HadUserActiveException("User with ID " + id + " is not banned");
        } else {
            throw new HadUserActiveException("User with ID " + id + " is not banned");
        }
    }

    /**
     * This function upgrades a user to premium status.
     * @param id The ID of the user to be upgraded to premium.
     */
    @Override
    @Transactional
    public void idPremiumUser(Long id) {
        validateUserId(id);
        User user = getUserEntityById(id);
        if (user.isPremium()) {
            throw new IdentityCardExistsException("User with ID " + id + " is already premium");
        } else {
            user.setPremium(true);
            userRepository.save(user);
        }
    }

    /**
     * This function removes premium status from a user.
     * @param id The ID of the user to be downgraded from premium.
     */
    @Override
    @Transactional
    public void removePremiumUser(Long id) {
        validateUserId(id);
        User user = getUserEntityById(id);
        if (!user.isPremium()) {
            throw new IdentityCardExistsException("User with ID " + id + " is not premium");
        } else {
            user.setPremium(false);
            userRepository.save(user);
        }
    }

    /**
     * This function retrieves a user by their ID.
     *
     * @param id - The ID of the user to retrieve.
     * @return UserResponseDto - The user details.
     * @brief This function retrieves a user by their ID.
     *
     */
    @Override
    public UserResponseDto getUserById(Long id) {
        return userMapper.toDto(getUserEntityById(id));
    }

    /**
     * @param pageable - The pagination information.
     * @return Page<UserResponseDto> - A paginated list of user details.
     * @brief This function retrieves a paginated list of all users.
     * *
     */
    @Override
    public List<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toDto).getContent();
    }

    /**
     * Retrieves a user by their username.
     * TODO: Chuẩn hoá username trước khi truy vấn
     *
     * @param username - The username of the user to retrieve.
     * @return UserResponseDto - The user details.
     */
    @Override
    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username.trim().toLowerCase());
        if (user == null) {
            throw new UsernameInvalidException("User with username " + username + " not found");
        }
        return userMapper.toDto(user);
    }

    /**
     * Retrieves a user by their identity card number.
     *
     * @param identityCard - The identity card number of the user to retrieve.
     * @return UserResponseDto - The user details.
     */
    @Override
    public UserResponseDto getUserByIdentityCard(String identityCard) {
        if (identityCard == null || identityCard.length() != 12 || !identityCard.matches("^\\d{12}$") || identityCard.trim().isEmpty()) {
            throw new IdentityCardInvalidException("Identity card cannot be null or empty");
        }
        return userMapper.toDto(userRepository.findByIdentityCard(identityCard));
    }

    /**
     * This function retrieves a user by their phone number.
     *
     * @param phoneNumber - The phone number of the user to retrieve.
     * @return UserResponseDto - The user details.
     */
    @Override
    public UserResponseDto getUserByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
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

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        User user = getUserEntityById(id);
        if (user.getUserStatus() == UserStatus.DELETED) {
            throw new HadUserDeteleException("User with ID " + id + " is already deleted");
        } else if (user.getUserStatus() == UserStatus.BANNED_PERMANENT) {
            throw new HadUserDeteleException("User with ID " + id + " is banned, cannot delete");
        }
        user.setUserStatus(UserStatus.DELETED);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameInvalidException("User with username " + username + " not found");
        }
        if (user.getUserStatus() == UserStatus.DELETED) {
            throw new HadUserDeteleException("User with username " + username + " is already deleted");
        } else if (user.getUserStatus() == UserStatus.BANNED_PERMANENT) {
            throw new HadUserDeteleException("User with username " + username + " is banned, cannot delete");
        }
        user.setUserStatus(UserStatus.DELETED);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUserByIdentityCard(String identityCard) {
        validateUserByIdentityCard(identityCard);
        UserResponseDto userResponseDto = getUserByIdentityCard(identityCard);
        User user = getUserEntityById(userResponseDto.getId());
        if (user.getUserStatus() == UserStatus.DELETED) {
            throw new HadUserDeteleException("User with identity card " + identityCard + " is already deleted");
        } else if (user.getUserStatus() == UserStatus.BANNED_PERMANENT) {
            throw new HadUserDeteleException("User with identity card " + identityCard + " is banned, cannot delete");
        }
        user.setUserStatus(UserStatus.DELETED);
        userRepository.save(user);
    }

    /**
     * This function retrieves the balance of a user by their ID.
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
     */
    @Override
    @Transactional
    public void decreaseBalance(Long id, BigDecimal amount) {
        validateAmount(amount);
        User user = getUserEntityById(id);
        user.setBalance(user.getBalance().subtract(amount));
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
        return userRepository.findById(id)
                .orElseThrow(() -> new UserIDNotFoundException("User with ID " + id + " not found"));
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
        // Chuẩn hoá email trước khi kiểm tra và truy vấn
        String normalized = email.trim().toLowerCase();
        if (!normalized.matches(emailRegex)) {
            throw new EmailInvalidException("Email is not valid");
        }
        UserResponseDto userResponseDto = userMapper.toDto(userRepository.findByEmail(normalized));
        if (userResponseDto == null) {
            throw new EmailNotFoundException("User with email " + normalized + " not found");
        }
        return userResponseDto;
    }

    /**
     * This function retrieves a user by their avatar URL.
     * *
     *
     * @param avatarUrl avatarUrl - The avatar URL of the user to retrieve.
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

    /**
     * * This function retrieves a set of users by their userStatus.
     * *
     *
     * @param status userStatus - The userStatus of the users to retrieve.
     * @return Set<UserResponseDto> - A set of user details.
     */
    @Override
    public Set<UserResponseDto> getUsersByStatus(String status) {
        return userMapper.toDtoSet(userRepository.findByUserStatus(UserStatus.valueOf(status.toUpperCase())));
    }

    /**
     * * This function retrieves a set of users by their social provider.
     * *
     *
     * @param provider provider - The social provider of the users to retrieve.
     * @return Set<UserResponseDto> - A set of user details.
     */
    @Override
    public Set<UserResponseDto> getUsersBySocialProvider(String provider) {
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("Provider cannot be null or empty");
        }
        try {
            AuthProvider authProvider = AuthProvider.valueOf(provider.toUpperCase());
            return userMapper.toDtoSet(userRepository.findByProvider(authProvider));
        } catch (IllegalArgumentException ex) {
            throw new UsernameInvalidException("Invalid social provider: " + provider);
        }
    }

    /**
     * Retrieves the current authenticated user's information from a database.
     * This method extracts username from the Spring Security Authentication context
     * and fetches fresh user data from a database.
     *
     * @param authentication The Spring Security authentication object containing user credentials
     * @return UserResponseDto containing the current user's latest information from a database
     * @throws AccessTokenInvalidException if authentication is null or principal is not UserDetails
     * @throws UsernameNotFoundException   if a user isn't found in a database
     */
    @Override
    public UserResponseDto getInfoUserCurrent(Authentication authentication) {
        // Validate authentication object
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessTokenInvalidException("Authentication is null or invalid");
        }

        // Extract username from authentication
        // Spring Security stores UserDetails in principle, which has getUsername() method
        String username;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new AccessTokenInvalidException("Invalid principal type: " + principal.getClass().getName());
        }

        // Fetch fresh user data from database
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User with username " + username + " not found");
        }

        return userMapper.toDto(user);
    }
}

