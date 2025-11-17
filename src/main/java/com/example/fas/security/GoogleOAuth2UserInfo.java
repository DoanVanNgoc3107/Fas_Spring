package com.example.fas.security;

import java.util.Map;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    /**
     * This function retrieves the provider ID from the attribute map.
     * @return The provider ID as a String.
     */
    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    /**
     * This function retrieves the email from the attribute map.
     * @return The email as a String.
     */
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    /**
     * This function retrieves the name from the attribute map.
     * @return The name as a String.
     */
    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getUsername() {
        // Google không có username, dùng email prefix hoặc name
        String email = (String) attributes.get("email");
        if (email != null && email.contains("@")) {
            return email.split("@")[0]; // Lấy phần trước @
        }
        return (String) attributes.get("name"); // Hoặc dùng name
    }

    /**
     * This function retrieves the avatar URL from the attribute map.
     * @return The avatar URL as a String.
     */
    @Override
    public String getAvatarUrl() {
        return (String) attributes.get("picture");
    }
}
