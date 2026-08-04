package com.escola.biblioteca.security;

import com.escola.biblioteca.dto.response.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

@Component
public class SecurityErrorWriter {

    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(MessageSource messageSource, ObjectMapper objectMapper) {
        this.messageSource = messageSource;
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String code)
            throws IOException {
        String message;
        try {
            message = messageSource.getMessage(code, new Object[0], RequestContextUtils.getLocale(request));
        } catch (NoSuchMessageException e) {
            message = code;
        }
        ApiError body = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                code, message, request.getRequestURI(), null);
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
