package com.portal.conecta.mapa_de_sala.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalHandlerException {

    private String path(HttpServletRequest request) {
        return request.getRequestURI();
    }
    
    private ResponseEntity<ApiError> buildResponse(
        HttpStatus status,
        RuntimeException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(ApiError.of(status, exception.getMessage(), path(request)));
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
            UnauthorizedUserException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception, request);
    }
}
