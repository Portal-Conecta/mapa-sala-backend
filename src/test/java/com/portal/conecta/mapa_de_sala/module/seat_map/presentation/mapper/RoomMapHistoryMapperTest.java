package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistoryAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoomMapHistoryMapperTest {

    private RoomMapHistoryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(RoomMapHistoryMapper.class);
    }

    @Test
    void toEntity_shouldMapRequestWithoutUserId() {
        var roomMapId = UUID.randomUUID();
        var request = new CreateRoomMapHistoryRequest(
                roomMapId,
                RoomMapHistoryAction.STUDENT_MOVED,
                "Aluno movido"
        );

        RoomMapHistory entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getRoomMapId()).isEqualTo(roomMapId);
        assertThat(entity.getAction()).isEqualTo(RoomMapHistoryAction.STUDENT_MOVED);
        assertThat(entity.getDetails()).isEqualTo("Aluno movido");
        assertThat(entity.getUserId()).isNull();
    }

    @Test
    void toResponse_shouldMapCreatedAt() {
        var entity = new RoomMapHistory();
        var id = UUID.randomUUID();
        var roomMap = new RoomMap();
        roomMap.setId(UUID.randomUUID());
        entity.setId(id);
        entity.setRoomMap(roomMap);
        entity.setUserId(UUID.randomUUID());
        entity.setAction(RoomMapHistoryAction.MAP_CREATION);
        entity.setDetails("Mapa criado");

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.action()).isEqualTo(RoomMapHistoryAction.MAP_CREATION);
        assertThat(response.details()).isEqualTo("Mapa criado");
    }

    @Test
    void toCommand_shouldAttachUserId() {
        var userId = UUID.randomUUID();
        var request = new CreateRoomMapHistoryRequest(
                UUID.randomUUID(),
                RoomMapHistoryAction.MAP_REPLICATED,
                "Mapa replicado"
        );

        CreateRoomMapHistoryCommand command = mapper.toCommand(request, userId);

        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.data()).isEqualTo(request);
    }
}
