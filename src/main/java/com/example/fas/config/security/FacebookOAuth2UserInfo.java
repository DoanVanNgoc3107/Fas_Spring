package com.example.fas.config.security;

import java.util.Map;

public class FacebookOAuth2UserInfo extends OAuth2UserInfo {

    public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    /**
     * This function retrieves the provider ID from the attribute map.
     * @return The provider ID as a String.
     */
    @Override
    public String getProviderId() {
        return (String) attributes.get("id");
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

    /**
     * This function retrieves the username from the attribute map.
     * @return The username as a String.
     */
    @Override
    public String getUsername() {
        return (String) attributes.get("login");
    }

    /**
     * This function retrieves the avatar URL from the attribute map.
     * @return The avatar URL as a String.
     */
    @Override
    public String getAvatarUrl() {
        return (String) attributes.get("avatar_url");
    }
}
