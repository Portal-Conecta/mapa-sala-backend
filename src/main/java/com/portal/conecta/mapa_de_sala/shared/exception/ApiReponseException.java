package com.portal.conecta.mapa_de_sala.shared.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;

public record ApiReponseException(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {

    public static ApiReponseException of(HttpStatus status, String message, String path) {
        return new ApiReponseException(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }
}
