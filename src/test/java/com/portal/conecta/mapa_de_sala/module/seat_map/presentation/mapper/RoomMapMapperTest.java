package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoomMapMapperTest {

    private RoomMapMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(RoomMapMapper.class);
    }

    @Test
    void toEntity_shouldMapHubForeignKeys() {
        var classId = UUID.randomUUID();
        var roomId = UUID.randomUUID();
        var templateId = UUID.randomUUID();
        var request = new CreateRoomMapRequest(classId, roomId, templateId);

        RoomMap entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getClassId()).isEqualTo(classId);
        assertThat(entity.getRoomId()).isEqualTo(roomId);
        assertThat(entity.getLayoutTemplateId()).isEqualTo(templateId);
    }

    @Test
    void applyUpdate_shouldUpdateLayoutTemplateId() {
        var entity = new RoomMap();
        var currentTemplate = new LayoutTemplate();
        currentTemplate.setId(UUID.randomUUID());
        entity.setLayoutTemplate(currentTemplate);
        var newTemplateId = UUID.randomUUID();

        mapper.applyUpdate(new UpdateRoomMapRequest(newTemplateId), entity);

        assertThat(entity.getLayoutTemplateId()).isEqualTo(newTemplateId);
    }

    @Test
    void toCommand_shouldCombineIdAndRequest() {
        var id = UUID.randomUUID();
        var request = new UpdateRoomMapRequest(UUID.randomUUID());

        UpdateRoomMapCommand command = mapper.toCommand(id, request);

        assertThat(command.id()).isEqualTo(id);
        assertThat(command.data()).isEqualTo(request);
    }
}
