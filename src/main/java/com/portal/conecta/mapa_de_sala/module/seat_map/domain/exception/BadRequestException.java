package com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}