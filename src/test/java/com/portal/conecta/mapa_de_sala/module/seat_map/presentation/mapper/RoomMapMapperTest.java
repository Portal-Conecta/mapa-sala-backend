package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.UpdateRoomMapCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomMapRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateRoomMapRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapResponse;
import org.junit.jupiter.api.Test;
import java.util.List;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoomMapMapperTest {

    private final RoomMapMapper mapper = new RoomMapMapperImpl();

    @Test
    void toEntity_shouldMapFieldsAndIgnoreIdAndTemplate() {
        var classId = UUID.randomUUID();
        var request = new CreateRoomMapRequest(classId, UUID.randomUUID(), UUID.randomUUID(), List.of());
        RoomMap entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getLayoutTemplate()).isNull();
        assertThat(entity.getClassId()).isEqualTo(classId);
    }

    @Test
    void toResponse_shouldExtractLayoutTemplateId() {
        var template = new LayoutTemplate();
        template.setId(UUID.randomUUID());

        var entity = new RoomMap();
        entity.setId(UUID.randomUUID());
        entity.setLayoutTemplate(template);

        RoomMapResponse response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(entity.getId());
        assertThat(response.layoutTemplateId()).isEqualTo(template.getId());
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