package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.UpdateLayoutTemplateCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateLayoutTemplateRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateLayoutTemplateRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutTemplateResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LayoutTemplateMapperTest {

    // Instancia a classe gerada pelo MapStruct no target
    private final LayoutTemplateMapper mapper = new LayoutTemplateMapperImpl();

    @Test
    void toEntity_shouldMapFieldsAndIgnoreId() {
        var request = new CreateLayoutTemplateRequest("Lab 1", 10, 10, true);

        LayoutTemplate entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Lab 1");
        assertThat(entity.getDimensionX()).isEqualTo(10);
        assertThat(entity.getDimensionY()).isEqualTo(10);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        var entity = new LayoutTemplate();
        entity.setId(UUID.randomUUID());
        entity.setName("Lab 2");
        entity.setDimensionX(5);
        entity.setDimensionY(5);
        entity.setActive(false);

        LayoutTemplateResponse response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(entity.getId());
        assertThat(response.name()).isEqualTo("Lab 2");
    }

    @Test
    void applyUpdate_shouldNotOverwriteWithNull() {
        var entity = new LayoutTemplate();
        entity.setName("Original Name");
        entity.setDimensionX(8);

        // Atualiza apenas a dimensão X, o nome vem null
        var request = new UpdateLayoutTemplateRequest(null, 15, null, null);

        mapper.applyUpdate(request, entity);

        assertThat(entity.getName()).isEqualTo("Original Name"); // Não sobrescreveu
        assertThat(entity.getDimensionX()).isEqualTo(15); // Atualizou
    }

    @Test
    void toCommand_shouldCombineIdAndRequest() {
        var id = UUID.randomUUID();
        var request = new UpdateLayoutTemplateRequest("Novo Lab", null, null, null);

        UpdateLayoutTemplateCommand command = mapper.toCommand(id, request);

        assertThat(command.id()).isEqualTo(id);
        assertThat(command.data()).isEqualTo(request);
    }
}