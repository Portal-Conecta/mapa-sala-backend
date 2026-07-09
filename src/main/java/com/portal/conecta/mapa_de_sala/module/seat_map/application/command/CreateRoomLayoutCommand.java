package com.portal.conecta.mapa_de_sala.module.seat_map.application.command;

import java.util.UUID;

public record CreateRoomLayoutCommand(UUID roomId, UUID layoutTemplateId) {
}
