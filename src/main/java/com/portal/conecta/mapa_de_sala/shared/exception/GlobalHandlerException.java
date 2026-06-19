package com.portal.conecta.mapa_de_sala.shared.exception;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    
    private ResponseEntity<ApiReponseException> buildResponse(
        HttpStatus status,
        RuntimeException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(ApiReponseException.of(status, exception.getMessage(), path(request)));
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<ApiReponseException> handleUnauthorized(
            UnauthorizedUserException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiReponseException> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, exception, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiReponseException> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler(RoomMapAlreadyArchivedException.class)
    public ResponseEntity<ApiReponseException> handleAlreadyArchived(
            RoomMapAlreadyArchivedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiReponseException> handleBadRequest(
            BadRequestException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiReponseException> handleConflict(
            ConflictException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception, request);
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ApiReponseException> handleInvalidPagination(
            InvalidPaginationException exception,
            HttpServletRequest request
    ){
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(InvalidLayoutPositionTypeException.class)
    public ResponseEntity<ApiReponseException> handleInvalidLayoutPositionType(
            InvalidLayoutPositionTypeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }
}
