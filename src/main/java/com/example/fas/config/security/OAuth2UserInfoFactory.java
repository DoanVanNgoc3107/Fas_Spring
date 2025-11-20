package com.example.fas.config.security;

import com.example.fas.model.enums.oauth2.AuthProvider;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.invalid.ProviderNotSupportException;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {

        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        return switch (provider) {
            case GITHUB -> new GithubOAuth2UserInfo(attributes);
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case FACEBOOK -> new FacebookOAuth2UserInfo(attributes);
            default -> throw new ProviderNotSupportException("Unsupported provider: " + registrationId);
        };
    }
}
