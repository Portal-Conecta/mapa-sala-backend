package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.UpdateLayoutPositionCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateLayoutPositionRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateLayoutPositionRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutPositionResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LayoutPositionMapperTest {

    private final LayoutPositionMapper mapper = new LayoutPositionMapperImpl();

    @Test
    void toEntity_shouldMapFieldsAndIgnoreIdAndTemplate() {
        var request = new CreateLayoutPositionRequest(UUID.randomUUID(), 1, 2, null);

        LayoutPosition entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getLayoutTemplate()).isNull(); // FK ignorada, será montada no Use Case
        assertThat(entity.getPositionX()).isEqualTo(1);
    }

    @Test
    void toResponse_shouldExtractLayoutTemplateId() {
        var template = new LayoutTemplate();
        template.setId(UUID.randomUUID());

        var entity = new LayoutPosition();
        entity.setId(UUID.randomUUID());
        entity.setLayoutTemplate(template);
        entity.setPositionX(5);

        LayoutPositionResponse response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(entity.getId());
        assertThat(response.layoutTemplateId()).isEqualTo(template.getId()); // Extraiu o ID da FK
    }

    @Test
    void applyUpdate_shouldNotOverwriteWithNull() {
        var entity = new LayoutPosition();
        entity.setPositionX(10);
        entity.setPositionY(10);

        var request = new UpdateLayoutPositionRequest(20, null, null);

        mapper.applyUpdate(request, entity);

        assertThat(entity.getPositionX()).isEqualTo(20);
        assertThat(entity.getPositionY()).isEqualTo(10);
    }

    @Test
    void toCommand_shouldCombineIdAndRequest() {
        var id = UUID.randomUUID();
        var request = new UpdateLayoutPositionRequest(1, 1, null);

        UpdateLayoutPositionCommand command = mapper.toCommand(id, request);

        assertThat(command.id()).isEqualTo(id);
        assertThat(command.data()).isEqualTo(request);
    }
}