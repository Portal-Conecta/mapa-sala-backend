package com.portal.conecta.mapa_de_sala.shared.exception;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiReponseException(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> errors
) {

    public static ApiReponseException of(HttpStatus status, String message, String path) {
        return new ApiReponseException(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                null
        );
    }

    public static ApiReponseException validation(
            HttpStatus status,
            String mensage,
            String path,
            List<FieldErrorDetail> errors
    ){
        return new ApiReponseException(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensage,
                path,
                errors
        );
    }

    public record FieldErrorDetail(
            String field,
            String message
    ){}
}
