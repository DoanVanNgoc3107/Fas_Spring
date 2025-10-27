package com.example.fas.security; // Hoặc package ...security.oauth2 của bạn

import com.example.fas.enums.Role;
import com.example.fas.enums.Social;
import com.example.fas.enums.Status;
import com.example.fas.exceptions.user.exists.AccountSocialExistsException;
import com.example.fas.exceptions.user.invalid.ProviderNotSupportException;
import com.example.fas.model.User;
import com.example.fas.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
// ✅ SỬA: Chúng ta sẽ trả về DefaultOAuth2User
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // ✅ Import helper kiểm tra String

import java.math.BigDecimal; // ✅ Import cho balance
import java.util.Map;
import java.util.Optional;

/**
 * LỚP PHIÊN DỊCH "CHUẨN":
 * Nhiệm vụ: Lấy dữ liệu thô từ GitHub,
 * "phiên dịch" nó thành User entity,
 * lưu vào CSDL, và trả User đó cho bước tiếp theo.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Đây là hàm "viết đè" (override)
     * Nó được gọi ngay sau khi "phi vụ gián điệp" (super.loadUser) thành công
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. Lấy "cục" dữ liệu thô (giống hệt code "gián điệp")
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. Lấy tên nhà cung cấp (vd: "github")
        String providerName = userRequest.getClientRegistration().getRegistrationId(); // -> "github"

        // 3. "Nhặt" các giá trị từ "cục" dữ liệu thô
        // (Đây là lý do chúng ta cần "phi vụ gián điệp"!)
        String providerSpecificId = getProviderSpecificId(attributes, providerName);
        String email = getEmail(attributes, providerName);
        String name = getName(attributes, providerName);
        String avatarUrl = getAvatarUrl(attributes, providerName);
        String username = getUsername(attributes, providerName); // 'login' của GitHub

        // 4. ✅ Logic "Tìm hoặc Tạo" (Find or Create)
        User user = processOAuth2User(providerName, providerSpecificId, email, name, avatarUrl, username);

        // 5. ✅ "ĐÓNG GÓI" VÀ TRẢ VỀ
        // Chúng ta tạo một "cục" dữ liệu mới để trả về
        // "Cục" này chứa quyền (authorities) và thông tin user

        // Tạo ra một map "attributes" MỚI và "sạch"
        // Quan trọng nhất: nhét User entity của chúng ta vào đây
        Map<String, Object> customAttributes = Map.of(
                "user", user, // ✅ Nhét User entity vào key "user"
                // Bạn có thể thêm các key khác nếu muốn
                "username", user.getUsername()
        );

        // "username" là "Name Attribute Key" - Spring Security sẽ dùng nó
        // để gọi authentication.getName()
        String nameAttributeKey = "username";

        // Trả về một DefaultOAuth2User (một implementation chuẩn của OAuth2User)
        return new DefaultOAuth2User(
                user.getRole().getAuthorities(), // Lấy quyền (vd: "ROLE_USER") từ hàm ta vừa thêm
                customAttributes,                // "Cục" dữ liệu mới (chứa User entity)
                nameAttributeKey                 // Khóa định danh
        );
    }

    /**
     * Logic "Tìm hoặc Tạo" (Find or Create)
     * Đây là trái tim của việc đồng bộ hóa CSDL
     */
    private User processOAuth2User(String providerName,
                                   String providerSpecificId,
                                   String email, String name,
                                   String avatarUrl,
                                   String username) {

        // Chuyển "github" (String) thành Social.GITHUB (Enum)
        Social provider = Social.valueOf(providerName.toUpperCase());

        // 1. HỎI CSDL: "Đã có user nào có provider=GITHUB và providerId=88123456 chưa?"
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, providerSpecificId);

        User user;

        if (userOptional.isPresent()) {
            // --- 2a. NẾU CÓ (Người quen!) ---
            user = userOptional.get();

            // Cập nhật thông tin (biết đâu họ đổi tên/avatar trên GitHub)
            if (StringUtils.hasText(name) && !name.equals(user.getFullName())) {
                user.setFullName(name);
            }
            if (StringUtils.hasText(avatarUrl) && !avatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarUrl);
            }
            // (Chúng ta thường không tự động cập nhật email, trừ khi có logic xác thực)

            userRepository.save(user);

        } else {
            // --- 2b. NẾU KHÔNG (Người mới!) ---

            // Xử lý trường hợp email bị trùng (ví dụ user LOCAL đã tồn tại)
            if (StringUtils.hasText(email) && userRepository.findByEmail(email) != null) {
                // Bạn có thể "link" (liên kết) tài khoản,
                // nhưng đơn giản nhất là báo lỗi
                throw new AccountSocialExistsException("Email đã được đăng ký bằng phương thức khác: " + email);
            }

            // Tạo một User entity mới
            user = new User();
            user.setProvider(provider);                 // vd: Social.GITHUB
            user.setProviderId(providerSpecificId);     // vd: "88123456"
            user.setFullName(name);                     // vd: "Doan Van Ngoc"
            user.setUsername(username);                 // vd: "DoanVanNgoc3107"
            user.setEmail(email);                       // vd: "ngoc.dev@example.com"
            user.setAvatarUrl(avatarUrl);               // vd: "https://..."
            user.setStatus(Status.ACTIVE);              // Mặc định là ACTIVE
            user.setRole(Role.USER);                    // Mặc định là USER
            user.setBalance(BigDecimal.ZERO);           // Mặc định balance = 0
            // Password, identityCard, phoneNumber sẽ là null (đúng như thiết kế)

            userRepository.save(user);
        }

        return user;
    }

    // --- CÁC HÀM "NHẶT" DỮ LIỆU ---
    // (Đây là các hàm helper để "nhặt" đúng key từ "cục" dữ liệu thô)

    private String getProviderSpecificId(Map<String, Object> attributes, String providerName) {
        if ("github".equalsIgnoreCase(providerName)) {
            // key "id" (kiểu Integer/Long), chuyển sang String
            return String.valueOf(attributes.get("id"));
        }
        // (Nếu sau này thêm Google)
        // if ("google".equalsIgnoreCase(providerName)) {
        //    return (String) attributes.get("sub"); // Google dùng key "sub"
        // }
        throw new ProviderNotSupportException("Provider không được hỗ trợ: " + providerName);
    }

    private String getEmail(Map<String, Object> attributes, String providerName) {
        if ("github".equalsIgnoreCase(providerName)) {
            return (String) attributes.get("email"); // key "email"
        }
        return null;
    }

    private String getName(Map<String, Object> attributes, String providerName) {
        if ("github".equalsIgnoreCase(providerName)) {
            return (String) attributes.get("name"); // key "name"
        }
        return null;
    }

    private String getAvatarUrl(Map<String, Object> attributes, String providerName) {
        if ("github".equalsIgnoreCase(providerName)) {
            return (String) attributes.get("avatar_url"); // key "avatar_url"
        }
        return null;
    }

    private String getUsername(Map<String, Object> attributes, String providerName) {
        if ("github".equalsIgnoreCase(providerName)) {
            return (String) attributes.get("login"); // key "login"
        }
        return null;
    }
}