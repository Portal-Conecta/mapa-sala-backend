package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LayoutPositionMapperTest {

    private LayoutPositionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(LayoutPositionMapper.class);
    }

    @Test
    void toEntity_shouldMapCoordinatesAndType() {
        var templateId = UUID.randomUUID();
        var request = new CreateLayoutPositionRequest(templateId, 2, 3, LayoutPositionType.STUDENT);

        LayoutPosition entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getLayoutTemplateId()).isEqualTo(templateId);
        assertThat(entity.getPositionX()).isEqualTo(2);
        assertThat(entity.getPositionY()).isEqualTo(3);
        assertThat(entity.getType()).isEqualTo(LayoutPositionType.STUDENT);
    }

    @Test
    void toResponse_shouldMapLayoutTemplateId() {
        var entity = new LayoutPosition();
        var id = UUID.randomUUID();
        var templateId = UUID.randomUUID();
        var template = new LayoutTemplate();
        template.setId(templateId);
        entity.setId(id);
        entity.setLayoutTemplate(template);
        entity.setPositionX(1);
        entity.setPositionY(1);
        entity.setType(LayoutPositionType.TEACHER);

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.layoutTemplateId()).isEqualTo(templateId);
        assertThat(response.type()).isEqualTo(LayoutPositionType.TEACHER);
    }

    @Test
    void toCommand_shouldCombineIdAndRequest() {
        var id = UUID.randomUUID();
        var request = new UpdateLayoutPositionRequest(4, 5, LayoutPositionType.EQUIPMENT);

        UpdateLayoutPositionCommand command = mapper.toCommand(id, request);

        assertThat(command.id()).isEqualTo(id);
        assertThat(command.data()).isEqualTo(request);
    }
}
