package com.example.fas.utils;

import com.example.fas.repositories.services.serviceImpl.exceptions.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomeAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * This function handles access denied exceptions by sending a JSON response with error details.
     *
     * @param request               - The HttpServletRequest object that contains the request the client made to the servlet
     * @param response              - The HttpServletResponse object that contains the response the servlet sends to the client
     * @param accessDeniedException - The AccessDeniedException that was thrown
     * @throws IOException      - If an input or output exception occurs
     * @throws ServletException - If a servlet-specific exception occurs
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                accessDeniedException.getClass().getSimpleName(),
                accessDeniedException.getMessage(),
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
