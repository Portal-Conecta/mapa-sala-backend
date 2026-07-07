package com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception;

import java.util.UUID;

public class RoomMapAlreadyArchivedException extends RuntimeException {

    public RoomMapAlreadyArchivedException(UUID id) {
        super("Mapa de sala com id %s já está arquivado.".formatted(id));
    }

    public RoomMapAlreadyArchivedException(String message) {
        super(message);
    }
}
