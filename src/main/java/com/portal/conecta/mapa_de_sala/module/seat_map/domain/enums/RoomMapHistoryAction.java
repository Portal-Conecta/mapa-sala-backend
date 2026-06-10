package com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums;

public enum RoomMapHistoryAction {
    MAP_CREATION, // mapa foi criado
    MAP_REPLICATED, // mapa foi replicado
    MAP_UPDATED, // mapa foi atualizado
    MAP_DELETED, // mapa foi deletado
    STUDENT_ASSIGNED, // aprendiz foi alocado para um lugar
    STUDENT_MOVED,       // movimento simples ou parte de substituição
    STUDENT_UNASSIGNED,  // perdeu lugar (DISPLACE)
    STUDENTS_SWAPPED    // troca entre dois aprendizes
}