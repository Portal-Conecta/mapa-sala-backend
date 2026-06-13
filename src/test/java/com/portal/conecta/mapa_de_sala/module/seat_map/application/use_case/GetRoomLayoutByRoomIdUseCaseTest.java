package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutPositionRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutPositionItemResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutTemplateWithPositionsResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper.LayoutTemplateMapper;

@ExtendWith(MockitoExtension.class)
class GetRoomLayoutByRoomIdUseCaseTest {

    @Mock
    private HubRoomPort hubRoomPort;

    @Mock
    private RoomLayoutRepository roomLayoutRepository;

    @Mock
    private LayoutTemplateRepository layoutTemplateRepository;

    @Mock
    private LayoutPositionRepository layoutPositionRepository;

    @Mock
    private LayoutTemplateMapper layoutTemplateMapper;

    private GetRoomLayoutByRoomIdUseCase useCase;

    private final UUID roomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID templateId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        useCase = new GetRoomLayoutByRoomIdUseCase(
                hubRoomPort,
                roomLayoutRepository,
                layoutTemplateRepository,
                layoutPositionRepository,
                layoutTemplateMapper
        );
    }

    @Test
    void execute_shouldReturnLayoutWithPositionsOrderedByYThenX() {
        when(hubRoomPort.existsById(roomId)).thenReturn(true);

        var roomLayout = new RoomLayout();
        roomLayout.setRoomId(roomId);
        roomLayout.setLayoutTemplateId(templateId);
        when(roomLayoutRepository.findByRoomId(roomId)).thenReturn(Optional.of(roomLayout));

        var template = new LayoutTemplate();
        template.setId(templateId);
        template.setDimensionX(10);
        template.setDimensionY(10);
        template.setActive(true);
        when(layoutTemplateRepository.findByIdAndActiveTrue(templateId)).thenReturn(Optional.of(template));

        var pos1 = position(2, 0, LayoutPositionType.STUDENT);
        var pos2 = position(1, 0, LayoutPositionType.STUDENT);
        var pos3 = position(0, 1, LayoutPositionType.TEACHER);
        var positions = List.of(pos2, pos1, pos3);
        when(layoutPositionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);

        var expectedResponse = new LayoutTemplateWithPositionsResponse(
                templateId,
                10,
                10,
                List.of(
                        new LayoutPositionItemResponse(1, 0, LayoutPositionType.STUDENT),
                        new LayoutPositionItemResponse(2, 0, LayoutPositionType.STUDENT),
                        new LayoutPositionItemResponse(0, 1, LayoutPositionType.TEACHER)
                )
        );
        when(layoutTemplateMapper.toWithPositionsResponse(template, positions)).thenReturn(expectedResponse);

        var response = useCase.execute(roomId);

        assertThat(response.layoutTemplateId()).isEqualTo(templateId);
        assertThat(response.dimensionX()).isEqualTo(10);
        assertThat(response.dimensionY()).isEqualTo(10);
        assertThat(response.positions()).hasSize(3);
        assertThat(response.positions().get(0).positionY()).isEqualTo(0);
        assertThat(response.positions().get(0).positionX()).isEqualTo(1);
        assertThat(response.positions().get(1).positionX()).isEqualTo(2);
        assertThat(response.positions().get(2).positionY()).isEqualTo(1);
        assertThat(response.positions().get(2).type()).isEqualTo(LayoutPositionType.TEACHER);
    }

    @Test
    void execute_shouldThrowWhenRoomDoesNotExistInHub() {
        when(hubRoomPort.existsById(roomId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(roomId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sala");
    }

    @Test
    void execute_shouldThrowWhenRoomHasNoLayoutAssigned() {
        when(hubRoomPort.existsById(roomId)).thenReturn(true);
        when(roomLayoutRepository.findByRoomId(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(roomId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Layout da sala");
    }

    @Test
    void execute_shouldThrowWhenTemplateIsInactive() {
        when(hubRoomPort.existsById(roomId)).thenReturn(true);

        var roomLayout = new RoomLayout();
        roomLayout.setLayoutTemplateId(templateId);
        when(roomLayoutRepository.findByRoomId(roomId)).thenReturn(Optional.of(roomLayout));
        when(layoutTemplateRepository.findByIdAndActiveTrue(templateId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(roomId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Template de layout");
    }

    private static LayoutPosition position(int x, int y, LayoutPositionType type) {
        var position = new LayoutPosition();
        position.setPositionX(x);
        position.setPositionY(y);
        position.setType(type);
        return position;
    }
}
