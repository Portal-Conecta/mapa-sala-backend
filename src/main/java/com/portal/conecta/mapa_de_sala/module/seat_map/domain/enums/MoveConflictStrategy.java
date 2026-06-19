package com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums;

public enum MoveConflictStrategy {
    DISPLACE, // o ocupante perde o lugar (hard delete do RoomMapLocation)
    SWAP      // os dois aprendizes trocam de posição (delete + recreate)
}