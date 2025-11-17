package com.example.fas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * Configuration for Spring Session JDBC
 * This ensures proper session management for OAuth2 login
 */
@Configuration
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 1800) // 30 minutes
public class SessionConfig extends AbstractHttpSessionApplicationInitializer {
    
    public SessionConfig() {
        super();
    }
}
