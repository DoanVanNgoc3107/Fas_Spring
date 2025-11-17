package com.example.fas.security; // Gói của bạn

import com.example.fas.enums.Role;
import com.example.fas.enums.Social;
import com.example.fas.enums.Status;
import com.example.fas.exceptions.user.exists.AccountSocialExistsException;
import com.example.fas.model.User;
import com.example.fas.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. Lấy "cục" dữ liệu thô (giữ nguyên)
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. Lấy tên nhà cung cấp (giữ nguyên)
        String providerName = userRequest.getClientRegistration().getRegistrationId(); // -> "github"

        // --- 3. SỰ THAY ĐỔI LỚN BẮT ĐẦU TỪ ĐÂY ---

        // Gọi "Quản đốc" (Factory) để tạo "Công nhân" (UserInfo)
        OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerName, attributes);

        // Giờ chúng ta lấy thông tin từ "công nhân" một cách thống nhất
        String providerSpecificId = oauth2UserInfo.getProviderId();
        String email = oauth2UserInfo.getEmail();
        String name = oauth2UserInfo.getName();
        String avatarUrl = oauth2UserInfo.getAvatarUrl();
        String username = oauth2UserInfo.getUsername();

        // 4. Logic "Tìm hoặc Tạo" (Find or Create)
        User user = processOAuth2User(providerName, providerSpecificId, email, name, avatarUrl, username);

        // 5. "ĐÓNG GÓI" VÀ TRẢ VỀ - CHỈ LƯU DATA CẦN THIẾT, KHÔNG LƯU ENTITY
        // Tránh serialization issues với Spring Session
        Map<String, Object> customAttributes = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "name", user.getFullName() != null ? user.getFullName() : "",
                "role", user.getRole().name(),
                "provider", user.getProvider().name());

        String nameAttributeKey = "username";

        return new DefaultOAuth2User(
                user.getRole().getAuthorities(),
                customAttributes,
                nameAttributeKey);
    }

    /**
     * Logic "Tìm hoặc Tạo" (Find or Create)
     * HÀM NÀY GẦN NHƯ Y HỆT BẢN GỐC CỦA BẠN.
     * Chúng ta chỉ thay đổi tên tham số một chút cho "chung chung" hơn.
     */
    private User processOAuth2User(String providerName,
            String providerSpecificId,
            String email, String name,
            String avatarUrl,
            String username) {

        Social provider = Social.valueOf(providerName.toUpperCase());

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, providerSpecificId);

        User user;

        // If user found
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (StringUtils.hasText(name) && !name.equals(user.getFullName())) {
                user.setFullName(name);
            }
            if (StringUtils.hasText(avatarUrl) && !avatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarUrl);
            }
            if (StringUtils.hasText(providerName) && user.getProvider().equals(Social.NONE)) {
                user.setProvider(provider);
            }
            if (StringUtils.hasText(providerSpecificId) && !providerSpecificId.equals(user.getProviderId())) {
                user.setProviderId(providerSpecificId);
            }
            if (StringUtils.hasText(email) && !email.equals(user.getEmail())) {
                user.setEmail(email);
            }

            userRepository.save(user);

        } else {
            // User not found by provider info, check for account linking by email
            User existingUser = null;
            if (StringUtils.hasText(email)) {
                existingUser = userRepository.findByEmail(email);
            }

            if (existingUser != null) {
                // Account linking: an account with this email already exists.
                // Link the new social provider to the existing account.
                user = existingUser;
                user.setProvider(provider);
                user.setProviderId(providerSpecificId);

                // Update name and avatar if they are different or missing
                if (StringUtils.hasText(name) && !name.equals(user.getFullName())) {
                    user.setFullName(name);
                }
                if (StringUtils.hasText(avatarUrl) && !avatarUrl.equals(user.getAvatarUrl())) {
                    user.setAvatarUrl(avatarUrl);
                }
                userRepository.save(user);
            } else {
                // New user registration
                String uniqueUsername = generateUniqueUsername(username, email, providerSpecificId);
                user = User.builder()
                        .fullName(name)
                        .username(uniqueUsername)
                        .password(null) // OAuth2 users do not have a password
                        .provider(provider)
                        .providerId(providerSpecificId)
                        .email(email)
                        .avatarUrl(avatarUrl)
                        .role(Role.USER)
                        .status(Status.ACTIVE)
                        .balance(BigDecimal.ZERO)
                        .build();
                userRepository.save(user);
            }
        }
        return user;
    }

    /**
     * Generate unique username for OAuth2 users
     * 
     * @param username   Username from OAuth2 provider (có thể null)
     * @param email      Email from OAuth2 provider
     * @param providerId Provider ID as fallback
     * @return Unique username (3-20 chars)
     */
    private String generateUniqueUsername(String username, String email, String providerId) {
        // Nếu username null hoặc empty, tạo từ email
        if (!StringUtils.hasText(username)) {
            if (StringUtils.hasText(email)) {
                username = email.split("@")[0];
            } else {
                // Fallback: dùng provider ID
                username = "user_" + providerId;
            }
        }

        // Trim về max 20 chars (vì validation yêu cầu max 20)
        if (username.length() > 20) {
            username = username.substring(0, 20);
        }

        // Ensure min 3 chars
        if (username.length() < 3) {
            username = username + "_" + providerId.substring(0, Math.min(3, providerId.length()));
        }

        // Check unique và add suffix nếu trùng
        String baseUsername = username;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            // Tạo suffix, đảm bảo không vượt quá 20 chars
            String suffix = "_" + counter;
            int maxBaseLength = 20 - suffix.length();
            username = baseUsername.substring(0, Math.min(baseUsername.length(), maxBaseLength)) + suffix;
            counter++;
        }
        return username;
    }
}