package com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}