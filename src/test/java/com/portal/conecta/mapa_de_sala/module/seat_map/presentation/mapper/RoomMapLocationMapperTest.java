package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoomMapLocationMapperTest {

    private RoomMapLocationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(RoomMapLocationMapper.class);
    }

    @Test
    void toEntity_shouldMapForeignKeys() {
        var roomMapId = UUID.randomUUID();
        var layoutPositionId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        var request = new CreateRoomMapLocationRequest(roomMapId, layoutPositionId, studentId);

        RoomMapLocation entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getRoomMapId()).isEqualTo(roomMapId);
        assertThat(entity.getLayoutPositionId()).isEqualTo(layoutPositionId);
        assertThat(entity.getStudentId()).isEqualTo(studentId);
    }

    @Test
    void toResponse_shouldExposeForeignKeysAsUuid() {
        var entity = new RoomMapLocation();
        var id = UUID.randomUUID();
        var roomMap = new RoomMap();
        roomMap.setId(UUID.randomUUID());
        var layoutPosition = new LayoutPosition();
        layoutPosition.setId(UUID.randomUUID());
        entity.setId(id);
        entity.setRoomMap(roomMap);
        entity.setLayoutPosition(layoutPosition);
        entity.setStudentId(UUID.randomUUID());

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.roomMapId()).isEqualTo(entity.getRoomMapId());
        assertThat(response.layoutPositionId()).isEqualTo(entity.getLayoutPositionId());
        assertThat(response.studentId()).isEqualTo(entity.getStudentId());
    }

    @Test
    void applyUpdate_shouldIgnoreNullFields() {
        var entity = new RoomMapLocation();
        var originalStudent = UUID.randomUUID();
        var layoutPosition = new LayoutPosition();
        layoutPosition.setId(UUID.randomUUID());
        entity.setStudentId(originalStudent);
        entity.setLayoutPosition(layoutPosition);

        var newStudent = UUID.randomUUID();
        mapper.applyUpdate(new UpdateRoomMapLocationRequest(null, newStudent), entity);

        assertThat(entity.getLayoutPositionId()).isEqualTo(layoutPosition.getId());
        assertThat(entity.getStudentId()).isEqualTo(newStudent);
    }

    @Test
    void toMoveCommand_shouldBuildMoveStudentCommand() {
        var roomMapId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        var targetPositionId = UUID.randomUUID();
        var request = new MoveStudentRequest(studentId, targetPositionId);

        MoveStudentCommand command = mapper.toMoveCommand(roomMapId, request);

        assertThat(command.roomMapId()).isEqualTo(roomMapId);
        assertThat(command.data()).isEqualTo(request);
    }
}
