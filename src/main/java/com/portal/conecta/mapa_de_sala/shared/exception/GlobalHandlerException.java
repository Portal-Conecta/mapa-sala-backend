package com.portal.conecta.mapa_de_sala.shared.exception;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalHandlerException {

    private String path(HttpServletRequest request) {
        return request.getRequestURI();
    }

    //401
    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<ApiReponseException> handleUnauthorized(
            UnauthorizedUserException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception, request);
    }

    //404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiReponseException> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, request);
    }

    //409
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiReponseException> handleConflict(
            ConflictException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception, request);
    }

    //400 - RN
    @ExceptionHandler({
            BadRequestException.class,
            RoomMapAlreadyArchivedException.class,
            InvalidPaginationException.class,
            InvalidLayoutPositionTypeException.class
    })
    public ResponseEntity<ApiReponseException> handleBadRequest(
            BadRequestException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    //400 - @Valid @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiReponseException> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ){
        return buildValidationResponse(exception.getBindingResult().getFieldErrors(), request);
    }

    // 400 — @Valid @ModelAttribute
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiReponseException> handleBind(
            BindException exception,
            HttpServletRequest request
    ) {
        return buildValidationResponse(exception.getBindingResult().getFieldErrors(), request);
    }

    // 400 — @NotNull em parâmetros de metodo
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiReponseException> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<ApiReponseException.FieldErrorDetail> errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String field = violation.getPropertyPath().toString();
                    if (field.contains(".")) {
                        field = field.substring(field.lastIndexOf('.') + 1);
                    }
                    return new ApiReponseException.FieldErrorDetail(field, violation.getMessage());
                })
                .toList();

        String message = errors.stream()
                .map(ApiReponseException.FieldErrorDetail::message)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Requisição inválida.");

        return ResponseEntity.badRequest()
                .body(ApiReponseException.validation(HttpStatus.BAD_REQUEST, message, path(request), errors));
    }

    // 400 — UUID inválido no path variable
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiReponseException> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        var message = new RuntimeException("Valor inválido para o parâmetro '%s'.".formatted(exception.getName()));
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // 400 — query param obrigatório ausente
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiReponseException> handleMissingParam(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        var message = new RuntimeException("O parâmetro obrigatório '%s' está ausente.".formatted(exception.getParameterName()));
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // 400 — JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiReponseException> handleNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.warn("Corpo da requisição inválido.", exception);

        var message = new RuntimeException("Corpo da requisição inválido.");

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // 409 — fallback de constraint do banco
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiReponseException> handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        log.warn("Violação de integridade de dados.", exception);

        var message = new RuntimeException("O recurso já existe.");
        return buildResponse(HttpStatus.CONFLICT, message, request);
    }

    // 500 — genérico
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiReponseException> handleRuntime(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        log.error("Erro inesperado: ", exception);

        var message = new RuntimeException("Ocorreu um erro inesperado.");
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiReponseException> handleGeneric(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Erro inesperado: ", exception);

        var message = new RuntimeException("Ocorreu um erro inesperado.");
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    @ExceptionHandler(RoomMapAlreadyArchivedException.class)
    public ResponseEntity<ApiReponseException> handleAlreadyArchived(
            RoomMapAlreadyArchivedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
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

    private ResponseEntity<ApiReponseException> buildResponse(
            HttpStatus status,
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(status, exception.getMessage(), request);
    }

    private ResponseEntity<ApiReponseException> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status)
                .body(ApiReponseException.of(status, message, path(request)));
    }

    private ResponseEntity<ApiReponseException> buildValidationResponse(
            List<FieldError> fieldErrors,
            HttpServletRequest request
    ){
        List<ApiReponseException.FieldErrorDetail> errors = fieldErrors.stream()
                .map(fieldError -> new ApiReponseException.FieldErrorDetail(
                        fieldError.getField(),
                        Objects.requireNonNull(fieldError.getDefaultMessage(), "Valor Inválido.")
                ))
                .toList();

        String message = errors.stream()
                .map(ApiReponseException.FieldErrorDetail::message)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Requisição Inválida.");

        return ResponseEntity.badRequest()
                .body(ApiReponseException.validation(HttpStatus.BAD_REQUEST, message, path(request), errors));
    }
}
