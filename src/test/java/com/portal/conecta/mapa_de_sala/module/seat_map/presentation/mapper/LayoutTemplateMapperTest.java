package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LayoutTemplateMapperTest {

    private LayoutTemplateMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(LayoutTemplateMapper.class);
    }

    @Test
    void toEntity_shouldMapCreateRequestAndIgnoreId() {
        var request = new CreateLayoutTemplateRequest("Laboratório", 10, 8, true);

        LayoutTemplate entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Laboratório");
        assertThat(entity.getDimensionX()).isEqualTo(10);
        assertThat(entity.getDimensionY()).isEqualTo(8);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        var entity = new LayoutTemplate();
        var id = UUID.randomUUID();
        entity.setId(id);
        entity.setName("Lab");
        entity.setDimensionX(5);
        entity.setDimensionY(4);
        entity.setActive(true);

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Lab");
        assertThat(response.dimensionX()).isEqualTo(5);
        assertThat(response.dimensionY()).isEqualTo(4);
        assertThat(response.active()).isTrue();
    }

    @Test
    void applyUpdate_shouldIgnoreNullFields() {
        var entity = new LayoutTemplate();
        entity.setName("Original");
        entity.setDimensionX(10);
        entity.setDimensionY(8);
        entity.setActive(true);

        mapper.applyUpdate(new UpdateLayoutTemplateRequest("Atualizado", null, null, null), entity);

        assertThat(entity.getName()).isEqualTo("Atualizado");
        assertThat(entity.getDimensionX()).isEqualTo(10);
        assertThat(entity.getDimensionY()).isEqualTo(8);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    void toCommand_shouldCombineIdAndRequest() {
        var id = UUID.randomUUID();
        var request = new UpdateLayoutTemplateRequest("Lab", 10, 8, true);

        UpdateLayoutTemplateCommand command = mapper.toCommand(id, request);

        assertThat(command.id()).isEqualTo(id);
        assertThat(command.data()).isEqualTo(request);
    }
}
