package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.MoveStudentCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.MoveStudentRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateRoomMapLocationRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapLocationResponse;

class RoomMapLocationMapperTest {

    private final RoomMapLocationMapper mapper = new RoomMapLocationMapperImpl();

    @Test
    void toResponse_shouldExtractIdsFromRelations() {
        var roomMap = new RoomMap();
        roomMap.setId(UUID.randomUUID());

        var position = new LayoutPosition();
        position.setId(UUID.randomUUID());

        var entity = new RoomMapLocation();
        entity.setId(UUID.randomUUID());
        entity.setRoomMap(roomMap);
        entity.setLayoutPosition(position);
        entity.setStudentId(UUID.randomUUID());

        RoomMapLocationResponse response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(entity.getId());
        assertThat(response.roomMapId()).isEqualTo(roomMap.getId());
        assertThat(response.layoutPositionId()).isEqualTo(position.getId());
        assertThat(response.studentId()).isEqualTo(entity.getStudentId());
    }

    @Test
    void applyUpdate_shouldNotOverwriteWithNull() {
        var entity = new RoomMapLocation();
        entity.setStudentId(UUID.randomUUID());

        var request = new UpdateRoomMapLocationRequest(UUID.randomUUID(), null);

        mapper.applyUpdate(request, entity);

        assertThat(entity.getStudentId()).isNotNull(); // Não foi sobrescrito
    }

    @Test
    void toMoveCommand_shouldCombineRoomMapIdAndRequest() {
        var roomMapId = UUID.randomUUID();
        var request = new MoveStudentRequest(UUID.randomUUID(), UUID.randomUUID(), null);

        MoveStudentCommand command = mapper.toMoveCommand(roomMapId, request);

        assertThat(command.roomMapId()).isEqualTo(roomMapId);
        assertThat(command.data()).isEqualTo(request);
    }
}