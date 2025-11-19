package com.example.fas.security; // Gói của bạn

import com.example.fas.enums.oauth2.AuthProvider;
import com.example.fas.enums.user.UserStatus;
import com.example.fas.exceptions.user.exists.AccountSocialExistsException;
import com.example.fas.model.Role;
import com.example.fas.model.User;
import com.example.fas.repositories.RoleRepository;
import com.example.fas.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Lấy thông tin người dùng từ nhà cung cấp OAuth2 (giữ nguyên)
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
                "role", user.getRole() != null ? user.getRole().getRoleName() : "",
                "provider", user.getProvider().name());

        String nameAttributeKey = "username";

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole() != null ? user.getRole().getRoleName() : "USER")),
                customAttributes,
                nameAttributeKey);
    }

    /**
     * Logic "Tìm hoặc Tạo" (Find or Create)
     */
    private User processOAuth2User(String providerName,
                                   String providerSpecificId,
                                   String email, String name,
                                   String avatarUrl,
                                   String username) {

        AuthProvider provider = AuthProvider.valueOf(providerName.toUpperCase());

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
            if (StringUtils.hasText(providerName) && user.getProvider().equals(AuthProvider.NONE)) {
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
            // Xử lý trường hợp email bị trùng (logic này vẫn đúng)
            if (StringUtils.hasText(email) && userRepository.findByEmail(email) != null) {
                throw new AccountSocialExistsException("Email had login as : " + email);
            }

            // Generate unique username nếu null hoặc quá dài
            String uniqueUsername = generateUniqueUsername(username, email, providerSpecificId);

            // Lấy role mặc định từ DB (USER)
            Role defaultRole = roleRepository.findByRoleName("USER").orElse(null);

            // Tạo một User entity mới
            user = User.builder()
                    .fullName(name)
                    .username(uniqueUsername)
                    .password(null) // OAuth2 users không có password
                    .provider(AuthProvider.valueOf(providerName.toUpperCase()))
                    .providerId(providerSpecificId)
                    .email(email)
                    .avatarUrl(avatarUrl)
                    .role(defaultRole)
                    .userStatus(UserStatus.ACTIVE)
                    .balance(BigDecimal.ZERO)
                    .build();
            userRepository.save(user);
        }
        return user;
    }

    private String generateUniqueUsername(String username, String email, String providerId) {
        if (!StringUtils.hasText(username)) {
            if (StringUtils.hasText(email)) {
                username = email.split("@")[0];
            } else {
                username = "user_" + providerId;
            }
        }

        if (username.length() > 20) {
            username = username.substring(0, 20);
        }

        if (username.length() < 3) {
            username = username + "_" + providerId.substring(0, Math.min(3, providerId.length()));
        }

        String baseUsername = username;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            String suffix = "_" + counter;
            int maxBaseLength = 20 - suffix.length();
            username = baseUsername.substring(0, Math.min(baseUsername.length(), maxBaseLength)) + suffix;
            counter++;
        }
        return username;
    }
}