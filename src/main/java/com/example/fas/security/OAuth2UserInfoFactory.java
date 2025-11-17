package com.example.fas.security;

import com.example.fas.enums.Social;
import com.example.fas.exceptions.user.invalid.ProviderNotSupportException;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {

        Social provider = Social.valueOf(registrationId.toUpperCase());

        return switch (provider) {
            case GITHUB -> new GithubOAuth2UserInfo(attributes);
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case FACEBOOK -> new FacebookOAuth2UserInfo(attributes);
            default -> throw new ProviderNotSupportException("Unsupported provider: " + registrationId);
        };
    }
}
