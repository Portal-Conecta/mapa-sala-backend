package com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;

import java.util.UUID;

public class InvalidLayoutPositionTypeException extends RuntimeException {

    public InvalidLayoutPositionTypeException(UUID id, LayoutPositionType actual) {
        super("Posição %s é do tipo %s e não pode ser usada para alocação de aprendiz.".formatted(id, actual));
    }

    public InvalidLayoutPositionTypeException(String message) {
        super(message);
    }
}
